package com.arsh.ashmare.skins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

final class MojangSkinClient {
	private static final Duration CACHE_TTL = Duration.ofHours(24);
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
	private static final String PROFILE_URL =
			"https://api.mojang.com/users/profiles/minecraft/";
	private static final String SESSION_URL =
			"https://sessionserver.mojang.com/session/minecraft/profile/";

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	private MojangSkinClient() {
	}

	static SkinResolution resolve(
			String lookupUsername,
			Optional<CachedSkin> cachedSkin
	) throws SkinApiException {
		long now = Instant.now().toEpochMilli();
		if (cachedSkin.isPresent() && isFresh(cachedSkin.get(), now)) {
			return new SkinResolution(cachedSkin.get(), null);
		}

		try {
			CachedSkin fetched = fetch(lookupUsername, now);
			return new SkinResolution(fetched, null);
		} catch (SkinApiException exception) {
			if (cachedSkin.isPresent()) {
				return new SkinResolution(
						cachedSkin.get(),
						lookupUsername + ": using cached texture because "
								+ exception.getMessage()
				);
			}
			throw exception;
		}
	}

	private static CachedSkin fetch(
			String lookupUsername,
			long fetchedAtEpochMillis
	) throws SkinApiException {
		JsonObject lookup = getJson(
				URI.create(PROFILE_URL + lookupUsername),
				"profile lookup for " + lookupUsername
		);
		String compactUuid = requiredString(lookup, "id", "profile lookup");
		String canonicalUsername = requiredString(
				lookup,
				"name",
				"profile lookup"
		);
		UUID sourceUuid = parseUuid(compactUuid);

		JsonObject sessionProfile = getJson(
				URI.create(SESSION_URL + compactUuid.replace("-", "")
						+ "?unsigned=false"),
				"session profile for " + lookupUsername
		);
		JsonArray properties = sessionProfile.getAsJsonArray("properties");
		if (properties == null) {
			throw new SkinApiException(
					"session profile for " + lookupUsername
							+ " did not contain properties"
			);
		}

		for (JsonElement propertyElement : properties) {
			if (!propertyElement.isJsonObject()) {
				continue;
			}
			JsonObject property = propertyElement.getAsJsonObject();
			if (!"textures".equals(optionalString(property, "name"))) {
				continue;
			}

			String value = requiredString(
					property,
					"value",
					"textures property"
			);
			String signature = requiredString(
					property,
					"signature",
					"textures property"
			);
			return new CachedSkin(
					lookupUsername,
					sourceUuid,
					canonicalUsername,
					new SkinTextureProperty(value, signature),
					fetchedAtEpochMillis
			);
		}

		throw new SkinApiException(
				"session profile for " + lookupUsername
						+ " did not contain a signed textures property"
		);
	}

	private static JsonObject getJson(
			URI uri,
			String description
	) throws SkinApiException {
		HttpRequest request = HttpRequest.newBuilder(uri)
				.timeout(REQUEST_TIMEOUT)
				.header("Accept", "application/json")
				.header("User-Agent", "Ashmare/1.0")
				.GET()
				.build();

		HttpResponse<String> response;
		try {
			response = HTTP_CLIENT.send(
					request,
					HttpResponse.BodyHandlers.ofString()
			);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new SkinApiException(description + " was interrupted", exception);
		} catch (IOException exception) {
			throw new SkinApiException(description + " failed", exception);
		}

		int status = response.statusCode();
		if (status == 204 || status == 404) {
			throw new SkinApiException(description + " was not found");
		}
		if (status != 200) {
			throw new SkinApiException(
					description + " returned HTTP " + status
			);
		}

		try {
			JsonElement parsed = JsonParser.parseString(response.body());
			if (!parsed.isJsonObject()) {
				throw new SkinApiException(
						description + " returned a non-object response"
				);
			}
			return parsed.getAsJsonObject();
		} catch (JsonParseException exception) {
			throw new SkinApiException(
					description + " returned invalid JSON",
					exception
			);
		}
	}

	private static boolean isFresh(CachedSkin cachedSkin, long now) {
		long age = now - cachedSkin.fetchedAtEpochMillis();
		return age >= 0 && age <= CACHE_TTL.toMillis();
	}

	private static String requiredString(
			JsonObject object,
			String field,
			String description
	) throws SkinApiException {
		String value = optionalString(object, field);
		if (value == null || value.isBlank()) {
			throw new SkinApiException(
					description + " did not contain " + field
			);
		}
		return value;
	}

	private static String optionalString(JsonObject object, String field) {
		JsonElement value = object.get(field);
		if (value == null || !value.isJsonPrimitive()) {
			return null;
		}
		return value.getAsString();
	}

	private static UUID parseUuid(String value) throws SkinApiException {
		String compact = value.replace("-", "");
		if (!compact.matches("[0-9a-fA-F]{32}")) {
			throw new SkinApiException("profile lookup returned an invalid UUID");
		}

		String dashed = compact.substring(0, 8)
				+ "-" + compact.substring(8, 12)
				+ "-" + compact.substring(12, 16)
				+ "-" + compact.substring(16, 20)
				+ "-" + compact.substring(20);
		return UUID.fromString(dashed);
	}

	record SkinResolution(CachedSkin skin, String warning) {
	}
}
