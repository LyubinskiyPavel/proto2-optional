package org.lpv.proto2plugin.plugin;

import com.google.protobuf.compiler.PluginProtos;

import java.util.stream.Stream;

public interface Extension {

    /**
     * Generate files for add new or change exists code files
     *
     * @param codeRequest request for generate new code
     * @return Stream of generated files
     */
    Stream<PluginProtos.CodeGeneratorResponse.File> generate(PluginProtos.CodeGeneratorRequest codeRequest);

}
