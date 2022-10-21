APP_ABI := arm64-v8a
APP_OPTIM := release
APP_CFLAGS += -O3 -ffunction-sections -fdata-sections -DLIBYUV_LEGACY_TYPES
APP_CPPFLAGS += -O3 -ffunction-sections -fdata-sections -fvisibility=hidden -fvisibility-inlines-hidden
APP_LDFLAGS += -Wl,--gc-sections
