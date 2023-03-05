
package com.broyden;

import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.textract.TextractClient;

/**
 * The module containing all dependencies required by the {@link Handler}.
 */
public class DependencyFactory {

    private DependencyFactory() {}

    /**
     * @return an instance of TextractClient
     */
    public static TextractClient textractClient() {
        return TextractClient.builder()
                       .httpClientBuilder(ApacheHttpClient.builder())
                       .build();
    }
}
