package com.hamusuke.twitter4mc.invoker;

import org.jetbrains.annotations.Nullable;

public interface TextHandlerInvoker {
    float getWidthWithEmoji(@Nullable String text);
}
