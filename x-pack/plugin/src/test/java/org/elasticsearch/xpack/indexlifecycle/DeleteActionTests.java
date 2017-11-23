/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.indexlifecycle;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.io.stream.Writeable.Reader;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.Index;
import org.elasticsearch.test.AbstractSerializingTestCase;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

public class DeleteActionTests extends AbstractSerializingTestCase<DeleteAction> {

    @Override
    protected DeleteAction doParseInstance(XContentParser parser) throws IOException {
        return DeleteAction.parse(parser);
    }

    @Override
    protected DeleteAction createTestInstance() {
        return new DeleteAction();
    }

    @Override
    protected Reader<DeleteAction> instanceReader() {
        return DeleteAction::new;
    }

    public void testExecute() throws Exception {
        Index index = new Index(randomAlphaOfLengthBetween(1, 20), randomAlphaOfLengthBetween(1, 20));

        Client client = Mockito.mock(Client.class);
        AdminClient adminClient = Mockito.mock(AdminClient.class);
        IndicesAdminClient indicesClient = Mockito.mock(IndicesAdminClient.class);

        Mockito.when(client.admin()).thenReturn(adminClient);
        Mockito.when(adminClient.indices()).thenReturn(indicesClient);
        Mockito.doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                DeleteIndexRequest request = (DeleteIndexRequest) invocation.getArguments()[0];
                @SuppressWarnings("unchecked")
                ActionListener<DeleteIndexResponse> listener = (ActionListener<DeleteIndexResponse>) invocation.getArguments()[1];
                assertNotNull(request);
                assertEquals(1, request.indices().length);
                assertEquals(index.getName(), request.indices()[0]);
                listener.onResponse(null);
                return null;
            }

        }).when(indicesClient).delete(Mockito.any(), Mockito.any());

        DeleteAction action = new DeleteAction();
        action.execute(index, client);

        Mockito.verify(client, Mockito.only()).admin();
        Mockito.verify(adminClient, Mockito.only()).indices();
        Mockito.verify(indicesClient, Mockito.only()).delete(Mockito.any(), Mockito.any());
    }

}
