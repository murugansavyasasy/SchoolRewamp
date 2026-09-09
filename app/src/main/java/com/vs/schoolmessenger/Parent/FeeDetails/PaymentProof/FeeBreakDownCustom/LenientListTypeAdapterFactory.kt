package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.FeeBreakDownCustom

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.lang.reflect.ParameterizedType

/**
 * Right now every "term" / "others" / "carryover" / "transport" / "hostel" /
 * "quantity" node in the API response is a JSON array. If the backend ever
 * sends a single object instead of a one-element array (a common API
 * inconsistency), the default Gson List adapter throws and the whole parse
 * fails. This factory intercepts every List<T> field and normalizes:
 *
 *   [ {...}, {...} ]   -> List of 2   (normal case, unchanged)
 *   { ... }            -> List of 1   (single object, wrapped)
 *   null / missing     -> empty list  (never a null list in your models)
 *
 * Register once when building your Retrofit/Gson instance:
 *
 *   val gson = GsonBuilder()
 *       .registerTypeAdapterFactory(LenientListTypeAdapterFactory())
 *       .create()
 */
class LenientListTypeAdapterFactory : TypeAdapterFactory {

    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (!List::class.java.isAssignableFrom(type.rawType)) return null

        val parameterizedType = type.type as? ParameterizedType ?: return null
        val elementType = parameterizedType.actualTypeArguments[0]
        val elementAdapter = gson.getAdapter(TypeToken.get(elementType))

        @Suppress("UNCHECKED_CAST")
        return LenientListAdapter(elementAdapter) as TypeAdapter<T>
    }

    private class LenientListAdapter<E>(
        private val elementAdapter: TypeAdapter<E>
    ) : TypeAdapter<List<E>>() {

        override fun write(out: JsonWriter, value: List<E>?) {
            out.beginArray()
            value?.forEach { elementAdapter.write(out, it) }
            out.endArray()
        }

        override fun read(reader: JsonReader): List<E> {
            return when (reader.peek()) {
                JsonToken.BEGIN_ARRAY -> {
                    val list = mutableListOf<E>()
                    reader.beginArray()
                    while (reader.hasNext()) {
                        list += elementAdapter.read(reader)
                    }
                    reader.endArray()
                    list
                }
                JsonToken.BEGIN_OBJECT -> {
                    // Single object where an array was expected — wrap it.
                    listOf(elementAdapter.read(reader))
                }
                JsonToken.NULL -> {
                    reader.nextNull()
                    emptyList()
                }
                else -> {
                    // Anything unexpected (e.g. an empty string "") — skip
                    // safely rather than crashing the whole response parse.
                    reader.skipValue()
                    emptyList()
                }
            }
        }
    }
}