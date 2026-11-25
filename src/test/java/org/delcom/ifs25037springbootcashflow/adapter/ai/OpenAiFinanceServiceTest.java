package org.delcom.ifs25037springbootcashflow.adapter.ai;

import com.sun.net.httpserver.HttpServer;
import org.delcom.ifs25037springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25037springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25037springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25037springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenAiFinanceServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private HttpServer server;
    private String serverUrl;
    private String mockResponseBody;
    private int mockResponseCode;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", exchange -> {
            byte[] bytes = mockResponseBody != null ? mockResponseBody.getBytes(StandardCharsets.UTF_8) : new byte[0];
            exchange.sendResponseHeaders(mockResponseCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
        serverUrl = "http://localhost:" + server.getAddress().getPort();
        mockResponseCode = 200;
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    /** Membungkus konten model menjadi respons chat completions yang valid. */
    private String wrap(String content) {
        return mapper.writeValueAsString(Map.of("choices", List.of(Map.of("message", Map.of("content", content)))));
    }

    private OpenAiFinanceService service() {
        return new OpenAiFinanceService(serverUrl, "valid-key", "test-model");
    }

    @Test
    void generatesTransactionsSuccessfully() throws Exception {
        mockResponseBody = wrap("[{\"description\":\"Gaji bulanan\",\"amount\":5000000,\"type\":\"INCOME\"},"
                + "{\"description\":\" Makan siang \",\"amount\":25000,\"type\":\"EXPENSE\"}]");

        List<AiGeneratedTransaction> result = service().generateTransactions("gajian dan makan");
        assertEquals(2, result.size());
        assertEquals("Gaji bulanan", result.get(0).description());
        assertEquals(5_000_000, result.get(0).amount());
        assertEquals(TransactionType.INCOME, result.get(0).type());
        assertEquals("Makan siang", result.get(1).description());
        assertEquals(TransactionType.EXPENSE, result.get(1).type());
    }

    @Test
    void acceptsTextAmountDecimalAmountAndIndonesianTypes() throws Exception {
        mockResponseBody = wrap("[{\"description\":\"A\",\"amount\":\"25000\",\"type\":\"pemasukan\"},"
                + "{\"description\":\"B\",\"amount\":1500.6,\"type\":\"Pengeluaran\"}]");

        List<AiGeneratedTransaction> result = service().generateTransactions("x");
        assertEquals(25_000, result.get(0).amount());
        assertEquals(TransactionType.INCOME, result.get(0).type());
        assertEquals(1501, result.get(1).amount());
        assertEquals(TransactionType.EXPENSE, result.get(1).type());
    }

    @Test
    void skipsInvalidItemsButKeepsValidOnes() throws Exception {
        mockResponseBody = wrap("[{\"amount\":1,\"type\":\"INCOME\"},"
                + "{\"description\":\"   \",\"amount\":1,\"type\":\"INCOME\"},"
                + "{\"description\":\"tanpa amount\",\"type\":\"INCOME\"},"
                + "{\"description\":\"amount teks\",\"amount\":\"lima ribu\",\"type\":\"INCOME\"},"
                + "{\"description\":\"amount nol\",\"amount\":0,\"type\":\"INCOME\"},"
                + "{\"description\":\"tanpa tipe\",\"amount\":10},"
                + "{\"description\":\"tipe aneh\",\"amount\":10,\"type\":\"TRANSFER\"},"
                + "{\"description\":\"Valid\",\"amount\":10,\"type\":\"EXPENSE\"}]");

        List<AiGeneratedTransaction> result = service().generateTransactions("x");
        assertEquals(1, result.size());
        assertEquals("Valid", result.get(0).description());
    }

    @Test
    void handlesMarkdownCodeFences() throws Exception {
        String json = "[{\"description\":\"Fence\",\"amount\":5,\"type\":\"INCOME\"}]";

        mockResponseBody = wrap("```json\n" + json + "\n```");
        assertEquals(1, service().generateTransactions("x").size());

        mockResponseBody = wrap("```json\n" + json);
        assertEquals(1, service().generateTransactions("x").size());

        mockResponseBody = wrap("```" + json + "```");
        assertEquals(1, service().generateTransactions("x").size());

        mockResponseBody = wrap("```" + json);
        assertEquals(1, service().generateTransactions("x").size());
    }

    @Test
    void normalizesEndpointVariantsAndFallbackModel() throws Exception {
        mockResponseBody = wrap("[{\"description\":\"E\",\"amount\":5,\"type\":\"INCOME\"}]");

        assertEquals(1, new OpenAiFinanceService(serverUrl + "/", "k", "   ").generateTransactions("x").size());
        assertEquals(1, new OpenAiFinanceService(serverUrl + "/chat/completions", "k", null)
                .generateTransactions("x").size());
        assertEquals(1, new OpenAiFinanceService(serverUrl, "k", " model ").generateTransactions("x").size());
    }

    @Test
    void rejectsInvalidInputs() {
        OpenAiFinanceService service = new OpenAiFinanceService("http://localhost", "valid-key", "model");
        assertThrows(ValidationException.class, () -> service.generateTransactions(null));
        assertThrows(ValidationException.class, () -> service.generateTransactions("   "));

        assertThrows(ValidationException.class,
                () -> new OpenAiFinanceService(null, "k", "m").generateTransactions("p"));
        assertThrows(ValidationException.class,
                () -> new OpenAiFinanceService("   ", "k", "m").generateTransactions("p"));
        assertThrows(ValidationException.class,
                () -> new OpenAiFinanceService("http://localhost", null, "m").generateTransactions("p"));
        assertThrows(ValidationException.class,
                () -> new OpenAiFinanceService("http://localhost", "   ", "m").generateTransactions("p"));
    }

    @Test
    void throwsOnHttpError() {
        mockResponseCode = 500;
        mockResponseBody = "Internal Server Error";

        DomainException ex = assertThrows(DomainException.class, () -> service().generateTransactions("x"));
        assertNotNull(ex.getMessage());
    }

    @Test
    void throwsOnMissingChoicesOrEmptyContent() {
        mockResponseBody = "{}";
        assertThrows(DomainException.class, () -> service().generateTransactions("x"));

        mockResponseBody = "{\"choices\": []}";
        assertThrows(DomainException.class, () -> service().generateTransactions("x"));

        mockResponseBody = "{\"choices\": [{}]}";
        assertThrows(DomainException.class, () -> service().generateTransactions("x"));

        mockResponseBody = "{\"choices\": [{\"message\": {}}]}";
        assertThrows(DomainException.class, () -> service().generateTransactions("x"));
    }

    @Test
    void throwsWhenContentIsNotJsonArrayOrHasNoValidItems() {
        mockResponseBody = wrap("{\"single\": 1}");
        assertThrows(DomainException.class, () -> service().generateTransactions("x"));

        mockResponseBody = wrap("[{\"description\":\"\",\"amount\":1,\"type\":\"INCOME\"}]");
        assertThrows(DomainException.class, () -> service().generateTransactions("x"));

        mockResponseBody = wrap("[]");
        assertThrows(DomainException.class, () -> service().generateTransactions("x"));

        mockResponseBody = wrap("invalid json syntax");
        assertThrows(DomainException.class, () -> service().generateTransactions("x"));
    }

    @Test
    void handlesHttpClientExceptions() throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        when(mockClient.send(any(HttpRequest.class), any())).thenThrow(new IOException("Connection refused"));

        OpenAiFinanceService service =
                new OpenAiFinanceService("http://localhost:1234", "key", "model", mockClient, new ObjectMapper());
        assertThrows(DomainException.class, () -> service.generateTransactions("Prompt"));

        when(mockClient.send(any(HttpRequest.class), any())).thenThrow(new InterruptedException("Interrupted"));
        assertThrows(DomainException.class, () -> service.generateTransactions("Prompt"));
        Thread.interrupted(); // bersihkan status interrupt yang di-set ulang oleh service
    }

    @Test
    void handlesSerializationError() {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Serialization failure"));

        OpenAiFinanceService service = new OpenAiFinanceService(
                "http://localhost:1234", "key", "model", HttpClient.newHttpClient(), mockMapper);
        assertThrows(DomainException.class, () -> service.generateTransactions("Prompt"));
    }
}
