package com.joyhubs.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * 向量数据配置
 * <p>
 * <code>spring.profiles.active=init</code>时进行向量数据初始化
 *
 * @author OxNPC
 */
@Slf4j
@Profile("init")
@Configuration
public class VectorStoreConfig {

    /**
     * Ingest the document into the vector store
     *
     * @param vectorStore
     * @param termsOfServiceDocs
     * @return
     */
    @Bean
    CommandLineRunner ingestTermOfServiceToVectorStore(VectorStore vectorStore,
                                                       @Value("classpath:rag/terms-of-service.txt") Resource termsOfServiceDocs) {
        return args -> {
            // 1. 先将资源包装成一个 Document 对象
            List<Document> documents = new TextReader(termsOfServiceDocs).read();
            Document document = documents.getFirst();
            String sha256Hex = DigestUtils.sha256Hex(document.getText());
            String filename = termsOfServiceDocs.getFilename();
            // Enrich Metadata
            document.getMetadata().put("fileName", filename);
            document.getMetadata().put("sha256", sha256Hex);

            // 2. TextSplitter 会在切分时自动将元数据传递给所有子块
            TextSplitter textSplitter = new TokenTextSplitter(
                    1000, // 每个块大约1000个 token
                    200,  // 相邻块之间重叠200个 token
                    5,    // 最小块大小
                    10000,// 最大块大小
                    true  // 是否保留换行符
            );
            List<Document> splitDocs = textSplitter.apply(documents);
            // 3. 写入带有元数据的文档列表
            vectorStore.add(splitDocs);

            log.info("Successfully ingested {} document chunks with metadata.", splitDocs.size());
        };
    }


}
