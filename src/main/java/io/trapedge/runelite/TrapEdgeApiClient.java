package io.trapedge.runelite;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TrapEdgeApiClient
{
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
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(config.apiBaseUrl() + "/api/plugin/bootstrap"))
			.GET()
			.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() >= 400)
		{
			throw new IOException("TrapEdge bootstrap request failed: " + response.statusCode());
		}

		return gson.fromJson(response.body(), TrapEdgeSnapshot.class);
	}

	ItemDetail loadItemDetail(int itemId) throws IOException, InterruptedException
	{
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(config.apiBaseUrl() + "/api/plugin/item/" + itemId))
			.GET()
			.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() >= 400)
		{
			throw new IOException("TrapEdge item request failed: " + response.statusCode());
		}

		return gson.fromJson(response.body(), ItemDetail.class);
	}
}
