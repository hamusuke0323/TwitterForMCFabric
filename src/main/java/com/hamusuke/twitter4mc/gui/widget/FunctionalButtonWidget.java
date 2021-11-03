package com.hamusuke.twitter4mc.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class FunctionalButtonWidget extends ButtonWidget {
    public final Function<Integer, Integer> yFunction;

    public FunctionalButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, Function<Integer, Integer> yFunction) {
        super(x, y, width, height, message, onPress);
        this.yFunction = yFunction;
    }

    public FunctionalButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, TooltipSupplier tooltipSupplier, Function<Integer, Integer> yFunction) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
        this.yFunction = yFunction;
    }


}
