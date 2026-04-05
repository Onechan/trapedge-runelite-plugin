package io.trapedge.runelite;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TrapEdgeApiClient
{
	static final String DEFAULT_HOSTED_BASE_URL = "https://onechan.github.io/trapedge-runelite-plugin/hosted-api";
	static final String RAW_HOSTED_BASE_URL = "https://raw.githubusercontent.com/Onechan/trapedge-runelite-plugin/main/hosted-api";
	static final String LOCAL_DEV_BASE_URL = "http://127.0.0.1:4311";

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final Gson gson = new Gson();
	private final TrapEdgeConfig config;

	@Inject
	public TrapEdgeApiClient(TrapEdgeConfig config)
	{
		this.config = config;
	}

	TrapEdgeSnapshot loadSnapshot() throws IOException, InterruptedException
	{
		return fetchJson(buildBootstrapUrls(), TrapEdgeSnapshot.class, "TrapEdge bootstrap request failed");
	}

	ItemDetail loadItemDetail(int itemId) throws IOException, InterruptedException
	{
		return fetchJson(buildItemUrls(itemId), ItemDetail.class, "TrapEdge item request failed");
	}

	String configuredBaseUrl()
	{
		return normalizeBaseUrl(config.apiBaseUrl());
	}

	String sourceSummary(TrapEdgeSnapshot snapshot)
	{
		if (snapshot != null && snapshot.sourceLabel != null && !snapshot.sourceLabel.isBlank())
		{
			return snapshot.sourceLabel + " — " + (snapshot.sourceBaseUrl != null ? snapshot.sourceBaseUrl : configuredBaseUrl());
		}
		if (isHostedStaticBase(configuredBaseUrl()))
		{
			return "Hosted read-only snapshot feed — " + configuredBaseUrl();
		}
		return "Custom API feed — " + configuredBaseUrl();
	}

	String recoveryHint()
	{
		return "Default source is hosted and read-only. For local live development, set API base URL to "
			+ LOCAL_DEV_BASE_URL + ".";
	}

	String failureStatusMessage(Exception ex)
	{
		return "Load failed: " + ex.getMessage();
	}

	private <T> T fetchJson(List<String> candidateUrls, Class<T> type, String errorPrefix) throws IOException, InterruptedException
	{
		IOException lastError = null;
		for (String candidateUrl : candidateUrls)
		{
			try
			{
				HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(candidateUrl))
					.GET()
					.build();

				HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() >= 400)
				{
					throw new IOException(errorPrefix + ": " + response.statusCode() + " @ " + candidateUrl);
				}

				return gson.fromJson(response.body(), type);
			}
			catch (IOException error)
			{
				lastError = error;
			}
		}

		if (lastError != null)
		{
			throw lastError;
		}
		throw new IOException(errorPrefix + ": no candidate URL available");
	}

	private List<String> buildBootstrapUrls()
	{
		String baseUrl = configuredBaseUrl();
		if (isHostedStaticBase(baseUrl))
		{
			return candidateUrls(baseUrl + "/bootstrap.json");
		}
		return candidateUrls(baseUrl + "/api/plugin/bootstrap");
	}

	private List<String> buildItemUrls(int itemId)
	{
		String baseUrl = configuredBaseUrl();
		if (isHostedStaticBase(baseUrl))
		{
			return candidateUrls(baseUrl + "/items/" + itemId + ".json");
		}
		return candidateUrls(baseUrl + "/api/plugin/item/" + itemId);
	}

	private List<String> candidateUrls(String primaryUrl)
	{
		Set<String> values = new LinkedHashSet<>();
		values.add(primaryUrl);
		if (primaryUrl.startsWith(DEFAULT_HOSTED_BASE_URL))
		{
			values.add(primaryUrl.replace(DEFAULT_HOSTED_BASE_URL, RAW_HOSTED_BASE_URL));
		}
		return new ArrayList<>(values);
	}

	private String normalizeBaseUrl(String value)
	{
		if (value == null || value.isBlank())
		{
			return DEFAULT_HOSTED_BASE_URL;
		}
		String normalized = value.trim();
		while (normalized.endsWith("/"))
		{
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}

	private boolean isHostedStaticBase(String baseUrl)
	{
		return baseUrl.contains("github.io") || baseUrl.contains("raw.githubusercontent.com") || baseUrl.endsWith("/hosted-api");
	}
}
