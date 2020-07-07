package org.lpv.proto2plugin.plugin;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptionalExtension implements Extension {

    private static final String EMPTY = "";

    @Override
    public Stream<PluginProtos.CodeGeneratorResponse.File> generate(PluginProtos.CodeGeneratorRequest codeRequest) {
        List<PluginProtos.CodeGeneratorResponse.File> generatedFiles = new ArrayList<>();
        for (DescriptorProtos.FileDescriptorProto fileDescriptor : codeRequest.getProtoFileList()) {
//            if (!"proto2".equals(fileDescriptor.getSyntax())) {
//                throw new UnsupportedOperationException();
//            }

            for (DescriptorProtos.DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
                List<DescriptorProtos.FieldDescriptorProto> optionalFields = filterOptionalFields(messageDescriptor);
                if (!optionalFields.isEmpty()) {
                    DescriptorProtos.FileOptions fileOptions = fileDescriptor.getOptions();
                    boolean multipleFiles = fileOptions.hasJavaMultipleFiles() ? fileOptions.getJavaMultipleFiles() : false;
                    generatedFiles.add(generateOptionalInterface(
                            multipleFiles,
                            getPackage(fileDescriptor),
                            messageDescriptor.getName(),
                            optionalFields
                    ));
                    generatedFiles.addAll(generateExtendOfOptionalInterface(
                            multipleFiles,
                            getPackage(fileDescriptor),
                            messageDescriptor.getName(),
                            optionalFields
                    ));
                }
            }
        }

        return generatedFiles.stream();
    }

    private List<PluginProtos.CodeGeneratorResponse.File> generateExtendOfOptionalInterface(boolean multipleFiles, String clazzPackage, String clazzName, List<DescriptorProtos.FieldDescriptorProto> optionalFields) {
        if (multipleFiles) {
            String targetJavaFile = String.format("%s/%s.java", clazzPackage.replaceAll("\\.", "/"), clazzName);
            return List.of(
                    PluginProtos.CodeGeneratorResponse.File.newBuilder()
                            .setName(targetJavaFile)
                            .setInsertionPoint(String.format("message_implements:%s.%s", clazzPackage, clazzName))
                            .setContent(String.format(" %s.%sOptional, ", clazzPackage, clazzName))
                            .build(),
                    PluginProtos.CodeGeneratorResponse.File.newBuilder()
                            .setName(targetJavaFile)
                            .setInsertionPoint(String.format("builder_implements:%s.%s", clazzPackage, clazzName))
                            .setContent(String.format(" %s.%sOptional, ", clazzPackage, clazzName))
                            .build()
            );
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private PluginProtos.CodeGeneratorResponse.File generateOptionalInterface(boolean multipleFiles, String clazzPackage, String clazzName, List<DescriptorProtos.FieldDescriptorProto> optionalFields) {
        if (multipleFiles) {
            return PluginProtos.CodeGeneratorResponse.File.newBuilder()
                    .setName(String.format("%s/%sOptional.java", clazzPackage.replaceAll("\\.", "/"), clazzName))
                    .setContent(InterfaceWriter.builder().packageName(clazzPackage).className(clazzName).fields(optionalFields).build().getCode())
                    .build();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private List<DescriptorProtos.FieldDescriptorProto> filterOptionalFields(DescriptorProtos.DescriptorProto messageDescriptor) {
        return messageDescriptor.getFieldList().stream()
                .filter(field -> field.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                .collect(Collectors.toList());
    }

    private String getPackage(DescriptorProtos.FileDescriptorProto fileDescriptor) {
        if (fileDescriptor.hasOptions() && fileDescriptor.getOptions().hasJavaPackage()) {
            return fileDescriptor.getOptions().getJavaPackage();
        }
        return fileDescriptor.hasPackage() ? fileDescriptor.getPackage() : EMPTY;
    }
}
