package com.sap.oss.phosphor.fosstars.data.github;

import static com.sap.oss.phosphor.fosstars.data.github.UseReuseDataProvider.REUSE_CONFIG;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.HAS_REUSE_LICENSES;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.IS_REUSE_COMPLIANT;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.README_HAS_REUSE_INFO;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.REGISTERED_IN_REUSE;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.USES_REUSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.sap.oss.phosphor.fosstars.model.Value;
import com.sap.oss.phosphor.fosstars.model.ValueSet;
import com.sap.oss.phosphor.fosstars.model.subject.oss.GitHubProject;
import com.sap.oss.phosphor.fosstars.model.value.ValueHashSet;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

public class UseReuseDataProviderTest extends TestGitHubDataFetcherHolder {

  private static final GitHubProject PROJECT = new GitHubProject("org", "test");

  @Test
  public void testSupportedFeature() {
    UseReuseDataProvider provider = new UseReuseDataProvider(fetcher);
    assertTrue(provider.supportedFeatures().contains(USES_REUSE));
    assertTrue(provider.supportedFeatures().contains(README_HAS_REUSE_INFO));
    assertTrue(provider.supportedFeatures().contains(HAS_REUSE_LICENSES));
    assertTrue(provider.supportedFeatures().contains(REGISTERED_IN_REUSE));
    assertTrue(provider.supportedFeatures().contains(IS_REUSE_COMPLIANT));
  }

  @Test
  public void testUseReuse() throws IOException {
    LocalRepository localRepository = mock(LocalRepository.class);
    TestGitHubDataFetcher.addForTesting(PROJECT, localRepository);

    when(localRepository.hasFile(REUSE_CONFIG)).thenReturn(false);
    Value<Boolean> value = UseReuseDataProvider.useReuse(PROJECT);
    assertFalse(value.isUnknown());
    assertFalse(value.get());

    when(localRepository.hasFile(REUSE_CONFIG)).thenReturn(true);
    value = UseReuseDataProvider.useReuse(PROJECT);
    assertEquals(USES_REUSE, value.feature());
    assertFalse(value.isUnknown());
    assertTrue(value.get());
  }

  @Test
  public void testReadmeHasReuseInfo() throws IOException {
    LocalRepository localRepository = mock(LocalRepository.class);
    TestGitHubDataFetcher.addForTesting(PROJECT, localRepository);

    when(localRepository.hasFile(anyString())).thenReturn(false);
    Value<Boolean> value = UseReuseDataProvider.readmeHasReuseInfo(PROJECT);
    assertEquals(README_HAS_REUSE_INFO, value.feature());
    assertFalse(value.isUnknown());
    assertFalse(value.get());

    when(localRepository.hasFile("README.md")).thenReturn(true);
    when(localRepository.file("README.md")).thenReturn(Optional.of("This is README."));
    value = UseReuseDataProvider.readmeHasReuseInfo(PROJECT);
    assertEquals(README_HAS_REUSE_INFO, value.feature());
    assertFalse(value.isUnknown());
    assertFalse(value.get());

    when(localRepository.hasFile("README.md")).thenReturn(true);
    when(localRepository.file("README.md"))
        .thenReturn(Optional.of(String.format(
            "Yes, README has a link to REUSE: https://api.reuse.software/info/github.com/%s/%s",
            PROJECT.organization().name(), PROJECT.name())));
    value = UseReuseDataProvider.readmeHasReuseInfo(PROJECT);
    assertEquals(README_HAS_REUSE_INFO, value.feature());
    assertFalse(value.isUnknown());
    assertTrue(value.get());
  }

  @Test
  public void testHasReuseLicenses() throws IOException {
    LocalRepository localRepository = mock(LocalRepository.class);
    TestGitHubDataFetcher.addForTesting(PROJECT, localRepository);

    Path licensesDirectory = Paths.get("LICENSES");

    when(localRepository.hasDirectory(licensesDirectory)).thenReturn(false);
    Value<Boolean> value = UseReuseDataProvider.hasReuseLicenses(PROJECT);
    assertEquals(HAS_REUSE_LICENSES, value.feature());
    assertFalse(value.isUnknown());
    assertFalse(value.get());

    when(localRepository.hasDirectory(licensesDirectory)).thenReturn(true);
    when(localRepository.files(any(), any())).thenReturn(Collections.emptyList());
    value = UseReuseDataProvider.hasReuseLicenses(PROJECT);
    assertEquals(HAS_REUSE_LICENSES, value.feature());
    assertFalse(value.isUnknown());
    assertFalse(value.get());

    when(localRepository.hasDirectory(licensesDirectory)).thenReturn(true);
    when(localRepository.files(any(), any()))
        .thenReturn(Collections.singletonList(licensesDirectory.resolve(Paths.get("LICENSE"))));
    value = UseReuseDataProvider.hasReuseLicenses(PROJECT);
    assertEquals(HAS_REUSE_LICENSES, value.feature());
    assertFalse(value.isUnknown());
    assertTrue(value.get());
  }

  @Test
  public void testReuseInfoWithError() throws IOException {
    testReuseInfo(404, null,
        ValueHashSet.from(REGISTERED_IN_REUSE.unknown(), IS_REUSE_COMPLIANT.unknown()));
  }

  @Test
  public void testReuseInfoWithUnregisteredProject() throws IOException {
    testReuseInfo(200, "unregistered",
        ValueHashSet.from(REGISTERED_IN_REUSE.value(false), IS_REUSE_COMPLIANT.value(false)));
  }

  @Test
  public void testReuseInfoWithNonCompliantProject() throws IOException {
    testReuseInfo(200, "non-compliant",
        ValueHashSet.from(REGISTERED_IN_REUSE.value(true), IS_REUSE_COMPLIANT.value(false)));
  }

  @Test
  public void testReuseInfoWithCompliantProject() throws IOException {
    testReuseInfo(200, "compliant",
        ValueHashSet.from(REGISTERED_IN_REUSE.value(true), IS_REUSE_COMPLIANT.value(true)));
  }

  public void testReuseInfo(int responseCode, String status, ValueSet expectedValues)
      throws IOException {

    StatusLine statusLine = mock(StatusLine.class);
    when(statusLine.getStatusCode()).thenReturn(responseCode);

    CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    when(response.getStatusLine()).thenReturn(statusLine);

    CloseableHttpClient client = mock(CloseableHttpClient.class);
    when(client.execute(any(HttpGet.class)))
        .thenReturn(response)
        .thenThrow(new AssertionError("Only one GET request was expected!"));

    HttpEntity entity = mock(HttpEntity.class);
    when(entity.getContent())
        .thenReturn(IOUtils.toInputStream(String.format("{ \"status\" : \"%s\"}", status)));

    statusLine = mock(StatusLine.class);
    when(statusLine.getStatusCode()).thenReturn(200);

    response = mock(CloseableHttpResponse.class);
    when(response.getStatusLine()).thenReturn(statusLine);
    when(response.getEntity()).thenReturn(entity);

    when(client.execute(any(HttpGet.class)))
        .thenReturn(response)
        .thenThrow(new AssertionError("Only one GET request was expected!"));

    UseReuseDataProvider provider = spy(new UseReuseDataProvider(fetcher));
    when(provider.httpClient()).thenReturn(client);

    ValueSet values = provider.fetchValuesFor(PROJECT);
    values.toSet().containsAll(expectedValues.toSet());
  }

}