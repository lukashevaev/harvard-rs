package com.ols.ruslan.neo;

public interface MediaTypeTransformerFacade {
    byte[] transform(byte[] content, String encoding) throws Exception;
}
