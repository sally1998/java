package com.pubnub.api.endpoints.channel_groups;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.awaitility.Awaitility;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsDeleteGroupResult;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsListAllResult;
import org.junit.Before;
import org.junit.Rule;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DeleteChannelGroupEndpointTest extends TestHarness {
    private DeleteChannelGroup partialDeleteChannelGroup;
    private PubNub pubnub;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void beforeEach() throws IOException {
        pubnub = this.createPubNubInstance(8080);
        partialDeleteChannelGroup = pubnub.deleteChannelGroup();
    }

    @org.junit.Test
    public void testSyncSuccess() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA/remove"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {}, \"service\": \"ChannelGroups\"}")));

        PNChannelGroupsDeleteGroupResult response = partialDeleteChannelGroup.channelGroup("groupA").sync();
        assertNotNull(response);
    }

    @org.junit.Test(expected = PubNubException.class)
    public void testSyncMissingGroup() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA/remove"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {}, \"service\": \"ChannelGroups\"}")));

        PNChannelGroupsDeleteGroupResult response = partialDeleteChannelGroup.sync();
    }

    @org.junit.Test(expected = PubNubException.class)
    public void testSyncEmptyGroup() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA/remove"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {}, \"service\": \"ChannelGroups\"}")));

        PNChannelGroupsDeleteGroupResult response = partialDeleteChannelGroup.channelGroup("").sync();
    }

    @org.junit.Test
    public void testIsAuthRequiredSuccessSync() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA/remove"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {}, \"service\": \"ChannelGroups\"}")));

        pubnub.getConfiguration().setAuthKey("myKey");
        PNChannelGroupsDeleteGroupResult response = partialDeleteChannelGroup.channelGroup("groupA").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myKey", requests.get(0).queryParameter("auth").firstValue());
    }

    @org.junit.Test
    public void testOperationTypeSuccessAsync() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA/remove"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {}, \"service\": \"ChannelGroups\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);

        partialDeleteChannelGroup.channelGroup("groupA").async(new PNCallback<PNChannelGroupsDeleteGroupResult>() {
            @Override
            public void onResponse(PNChannelGroupsDeleteGroupResult result, PNStatus status) {
                if (status != null && status.getOperation()==PNOperationType.PNRemoveGroupOperation) {
                    atomic.incrementAndGet();
                }
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

}
