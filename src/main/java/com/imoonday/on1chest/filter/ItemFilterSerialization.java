package com.imoonday.on1chest.filter;

import com.google.gson.*;
import net.minecraft.util.Identifier;

import java.lang.reflect.Type;

public class ItemFilterSerialization implements JsonSerializer<ItemFilter>, JsonDeserializer<ItemFilter> {

    @Override
    public ItemFilter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String id = json.getAsJsonObject().get("id").getAsString();
        Identifier identifier = Identifier.tryParse(id);
        return identifier != null ? ItemFilterManager.getFilter(identifier).orElse(new UnknownFilter(identifier)) : null;
    }

    @Override
    public JsonElement serialize(ItemFilter src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("id", src.getId().toString());
        return json;
    }
}
