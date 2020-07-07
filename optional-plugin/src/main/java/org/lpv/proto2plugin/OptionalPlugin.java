package org.lpv.proto2plugin;

import com.google.protobuf.compiler.PluginProtos;
import org.lpv.proto2plugin.plugin.Extension;
import org.lpv.proto2plugin.plugin.OptionalExtension;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class OptionalPlugin {

    private static final List<Extension> EXTENSIONS = List.of(new OptionalExtension());

    public static void main(String[] args) throws IOException {
        PluginProtos.CodeGeneratorRequest codeRequest = PluginProtos.CodeGeneratorRequest.parseFrom(System.in);

        PluginProtos.CodeGeneratorResponse codeResponse;
        try {
            List<PluginProtos.CodeGeneratorResponse.File> generatedFiles = EXTENSIONS.stream()
                    .flatMap(extension -> extension.generate(codeRequest))
                    .collect(Collectors.toList());

            codeResponse = PluginProtos.CodeGeneratorResponse.newBuilder()
                    .addAllFile(generatedFiles)
                    .build();

        } catch (Exception e) {
            e.printStackTrace(System.err);
            codeResponse = PluginProtos.CodeGeneratorResponse.newBuilder()
                    .setError(e.getMessage())
                    .build();
        }


        System.err.println(codeResponse);

        codeResponse.writeTo(System.out);
    }
}
