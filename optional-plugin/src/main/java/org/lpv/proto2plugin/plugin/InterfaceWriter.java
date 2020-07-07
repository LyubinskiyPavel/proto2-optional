package org.lpv.proto2plugin.plugin;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos;
import com.squareup.javapoet.*;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.*;
import static org.apache.commons.lang3.StringUtils.capitalize;

@Builder
public class InterfaceWriter {

    private static final Map<DescriptorProtos.FieldDescriptorProto.Type, Class<?>> typeToClassMap = ImmutableMap.<DescriptorProtos.FieldDescriptorProto.Type, Class<?>>builder()
            .put(TYPE_DOUBLE, Double.class)
            .put(TYPE_FLOAT, Float.class)
            .put(TYPE_INT64, Long.class)
            .put(TYPE_UINT64, Long.class)
            .put(TYPE_INT32, Integer.class)
            .put(TYPE_FIXED64, Long.class)
            .put(TYPE_FIXED32, Integer.class)
            .put(TYPE_BOOL, Boolean.class)
            .put(TYPE_STRING, String.class)
            .put(TYPE_UINT32, Integer.class)
            .put(TYPE_SFIXED32, Integer.class)
            .put(TYPE_SINT32, Integer.class)
            .put(TYPE_SFIXED64, Long.class)
            .put(TYPE_SINT64, Long.class)
            .build();

    private final String packageName;
    private final String className;
    private final List<DescriptorProtos.FieldDescriptorProto> fields;

    public String getCode() {
        List<MethodSpec> methods = fields.stream().map(field -> {
            ClassName fieldClass;
            if (typeToClassMap.containsKey(field.getType())) {
                fieldClass = ClassName.get(typeToClassMap.get(field.getType()));
            } else {
                int lastIndexOf = StringUtils.lastIndexOf(field.getTypeName(), '.');
                fieldClass = ClassName.get(field.getTypeName().substring(1, lastIndexOf), field.getTypeName().substring(lastIndexOf + 1));
            }

            return MethodSpec.methodBuilder(field.getName())
                    .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), fieldClass))
                    .addStatement("return has$N() ? $T.of(get$N()) : $T.empty()", capitalize(field.getName()), Optional.class, capitalize(field.getName()), Optional.class)
                    .build();
        }).collect(Collectors.toList());

        TypeSpec generatedInterface = TypeSpec.interfaceBuilder(className + "Optional")
                .addSuperinterface(ClassName.get(packageName, className + "OrBuilder"))
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methods)
                .build();

        return JavaFile.builder(packageName, generatedInterface).build().toString();
    }

    public static void main(String[] args) {
        InterfaceWriter person = InterfaceWriter.builder()
                .packageName("foo.bar")
                .className("Person")
                .fields(List.of(
                        DescriptorProtos.FieldDescriptorProto.newBuilder()
                                .setName("number")
                                .setType(TYPE_INT32)
                                .setTypeName("int32")
                                .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                                .build()
                ))
                .build();

        System.out.println(person.getCode());
    }
}
