package com.g2rain.model.nexus.protocol;


import com.g2rain.common.utils.Asserts;
import com.g2rain.model.nexus.enums.ModelProtocol;
import com.g2rain.model.nexus.enums.NexusErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link ModelClient} 注册与路由中心。
 * <p>
 * Spring 启动时注入容器中全部 {@link ModelClient} Bean，按
 * {@link ModelClient#protocol()} 建立 {@code Map}；业务侧通过存库协议字符串
 * 调用 {@link #require(String)} 取得对应实现。
 * <p>
 * 若同一协议存在多个实现，后者覆盖前者（{@code (a, b) -> b}）。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Component
public class ModelClientRegistry {

    /**
     * 协议 -> 客户端实现；构造时一次性构建，运行期只读。
     */
    private final Map<ModelProtocol, ModelClient> clientsByProtocol;

    /**
     * 收集全部 {@link ModelClient} 并按协议建索引。
     *
     * @param clients Spring 注入的全部协议客户端实现（OpenAI / Anthropic / Gemini 等）
     */
    public ModelClientRegistry(List<ModelClient> clients) {
        // Stream 收集为 Map：key = 各实现声明的 protocol()，value = 实现本身
        // merge 函数 (a, b) -> b：同一协议若误注册多个 Bean，保留后注册的那个
        this.clientsByProtocol = clients.stream().collect(Collectors.toMap(
            ModelClient::protocol, Function.identity(), (_, b) -> b)
        );
    }

    /**
     * 按协议名获取客户端；协议无法识别或未注册实现时抛业务异常。
     *
     * @param protocol 存库协议字符串（与 {@link ModelProtocol#name()} 忽略大小写匹配）
     * @return 对应协议的 {@link ModelClient}，不为 null
     */
    public ModelClient require(String protocol) {
        // 第一步：把存库字符串解析成枚举（忽略大小写）；解析失败得到 null
        ModelProtocol modelProtocol = ModelProtocol.nameOf(protocol);
        // 无法识别的协议名（如拼写错误、未定义枚举）：直接业务失败
        Asserts.notNull(modelProtocol, NexusErrorCode.PROTOCOL_UNSUPPORTED, protocol);

        // 第二步：用枚举从注册表取实现
        ModelClient client = clientsByProtocol.get(modelProtocol);
        // 枚举存在但启动时没有对应 @Component 实现：同样按不支持处理
        Asserts.notNull(client, NexusErrorCode.PROTOCOL_UNSUPPORTED, protocol);

        // 第三步：返回可用客户端，供上层 createChatModel / createEmbeddingModel
        return client;
    }
}
