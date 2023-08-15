package com.imoonday.on1chest.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.imoonday.on1chest.screen.client.StorageAssessorScreen;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.*;

@Environment(EnvType.CLIENT)
public class ScreenConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File file;
    private static ScreenConfig INSTANCE = new ScreenConfig();

    public KeyBinding markItemStackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.on1chest.mark_item_stack", GLFW.GLFW_KEY_C, "key.categories.on1chest"));
    public KeyBinding takeAllStacksKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.on1chest.take_all_stacks", GLFW.GLFW_KEY_SPACE, "key.categories.on1chest"));
    public boolean displaySortWidget;
    public int sortWidgetOffsetX;
    public int sortWidgetOffsetY;
    public boolean displayCheckBoxes;
    public int checkBoxesOffsetX;
    public int checkBoxesOffsetY;

    private static void prepareConfigFile() {
        if (file == null) {
            file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "on1chest.json");
        }
    }

    public static void initConfig() {
        load();
    }

    public static void load() {
        prepareConfigFile();

        try {
            if (!file.exists()) {
                save();
            }
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                JsonElement jsonElement = JsonParser.parseReader(br);
                ScreenConfig config = ScreenConfig.fromJson(jsonElement.toString());
                if (config != null) {
                    ScreenConfig.INSTANCE = config;
                } else {
                    save();
                }
            }
        } catch (Exception e) {
            System.err.println("Couldn't load on1chest configuration file; reverting to defaults");
            e.printStackTrace();
            save();
        }
    }

    public static boolean save() {
        prepareConfigFile();
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(getInstance().toJson());
            return true;
        } catch (IOException e) {
            System.err.println("Couldn't save on1chest configuration file");
            e.printStackTrace();
            return false;
        }
    }

    public static ScreenConfig getInstance() {
        return INSTANCE;
    }

    @Environment(EnvType.CLIENT)
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("On1chest"))
                .setSavingRunnable(() -> {
                    if (MinecraftClient.getInstance().currentScreen instanceof StorageAssessorScreen screen) {
                        screen.onScreenConfigUpdate(ScreenConfig.INSTANCE);
                    }
                    save();
                });

        ConfigCategory screenSettings = builder.getOrCreateCategory(Text.literal("Screen Settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        screenSettings.addEntry(entryBuilder.fillKeybindingField(Text.translatable(getInstance().markItemStackKey.getTranslationKey()), getInstance().markItemStackKey)
                .setTooltip(Text.literal("按住键点击物品进行标记收藏"))
                .setKeySaveConsumer(key -> {
                    getInstance().markItemStackKey.setBoundKey(key);
                    KeyBinding.updateKeysByCode();
                })
                .build());

        screenSettings.addEntry(entryBuilder.fillKeybindingField(Text.translatable(getInstance().takeAllStacksKey.getTranslationKey()), getInstance().takeAllStacksKey)
                .setTooltip(Text.literal("按住键点击物品拿取全部"))
                .setKeySaveConsumer(key -> {
                    getInstance().takeAllStacksKey.setBoundKey(key);
                    KeyBinding.updateKeysByCode();
                })
                .build());

        screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.literal("显示排序按钮"), INSTANCE.displaySortWidget)
                .setDefaultValue(true)
                .setSaveConsumer(display -> getInstance().displaySortWidget = display)
                .build());

        screenSettings.addEntry(entryBuilder.startIntField(Text.literal("排序按钮X偏移"), INSTANCE.sortWidgetOffsetX)
                .setDefaultValue(0)
                .setSaveConsumer(offset -> getInstance().sortWidgetOffsetX = offset)
                .build());

        screenSettings.addEntry(entryBuilder.startIntField(Text.literal("排序按钮Y偏移"), INSTANCE.sortWidgetOffsetY)
                .setDefaultValue(0)
                .setSaveConsumer(offset -> getInstance().sortWidgetOffsetY = offset)
                .build());

        screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.literal("显示过滤选项"), INSTANCE.displayCheckBoxes)
                .setDefaultValue(true)
                .setSaveConsumer(display -> getInstance().displayCheckBoxes = display)
                .build());

        screenSettings.addEntry(entryBuilder.startIntField(Text.literal("过滤选项X偏移"), INSTANCE.checkBoxesOffsetX)
                .setDefaultValue(0)
                .setSaveConsumer(offset -> getInstance().checkBoxesOffsetX = offset)
                .build());

        screenSettings.addEntry(entryBuilder.startIntField(Text.literal("过滤选项Y偏移"), INSTANCE.checkBoxesOffsetY)
                .setDefaultValue(0)
                .setSaveConsumer(offset -> getInstance().checkBoxesOffsetY = offset)
                .build());

        return builder.build();
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    @Nullable
    public static ScreenConfig fromJson(String json) {
        return GSON.fromJson(json, ScreenConfig.class);
    }
}
