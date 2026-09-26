package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

/**
 * 作物定义来源。JSON 重载时只替换 JSON 条目，不会清掉代码注册或自动发现的作物。
 */
public enum CropOrigin {
    API,
    JSON,
    DISCOVERY,
    CUSTOM
}
