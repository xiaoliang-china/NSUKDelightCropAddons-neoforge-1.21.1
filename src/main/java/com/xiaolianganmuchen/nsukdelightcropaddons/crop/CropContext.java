package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import com.xiaolianganmuchen.nsukdelightcropaddons.NSUKDelightCropAddons;
import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.FarmlandBoxDataAccess;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * 服务端农民作业时的当前乐事作物。
 * NSUK 的 {@code FarmCrop} 是枚举，附属模组不能追加常量，因此在 tick 一个农田盒时把解析结果放进线程上下文，
 * 再由 Mixin 把 {@code seed/plantState/isMatureFull} 等方法转到这里。
 * {@link ArrayDeque} 禁止 null，原版农田盒必须压入 {@link ResolvedCrop#missing()} 而不是 null。
 */
public final class CropContext {
    private static final ThreadLocal<Deque<ResolvedCrop>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private CropContext() {
    }

    public static void push(FarmlandBoxData data) {
        STACK.get().push(resolveOrMissing(data));
    }

    public static void pop() {
        Deque<ResolvedCrop> stack = STACK.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            STACK.remove();
        }
    }

    public static void clear() {
        STACK.remove();
    }

    public static Optional<ResolvedCrop> current() {
        Deque<ResolvedCrop> stack = STACK.get();
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        ResolvedCrop crop = stack.peek();
        if (crop == null || crop.isMissing()) {
            return Optional.empty();
        }
        return Optional.of(crop);
    }

    private static ResolvedCrop resolveOrMissing(FarmlandBoxData data) {
        try {
            String addonId = FarmlandBoxDataAccess.addonCropId(data);
            if (addonId == null || addonId.isBlank()) {
                return ResolvedCrop.missing();
            }
            return CropCatalog.resolve(addonId).orElse(ResolvedCrop.missing());
        } catch (Exception exception) {
            NSUKDelightCropAddons.LOGGER.error("Failed to bind delight crop context for farmland box", exception);
            return ResolvedCrop.missing();
        }
    }
}
