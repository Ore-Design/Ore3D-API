package design.ore.api.ore3d.plugin.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import design.ore.api.ore3d.ApiConstants;
import design.ore.api.ore3d.plugin.IOre3DPlugin;
import design.ore.api.ore3d.plugin.annotation.Ore3DPlugin;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes({"design.ore.api.ore3d.plugin.annotation.Ore3DPlugin"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class Ore3DPluginAnnotationProcessor extends AbstractProcessor
{
    public static final String OUTPUT_FILE = "META-INF/ore3d-manifest.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if(annotations.isEmpty()) return false;

        Ore3DPluginManifest manifest = new Ore3DPluginManifest();

        try {
            for (TypeElement annotation : annotations) {
                if (isPluginAnnotation(annotation)) {
                    for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                        if (element.getKind() != ElementKind.CLASS) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Plugin annotation can only be applied to classes.", element);
                            continue;
                        }

                        TypeElement classElement = (TypeElement) element;
                        if (!implementsOre3DPluginInterface(classElement)) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Annotated class must implement IOre3DPlugin.", classElement);
                            continue;
                        }

                        String fullClassName = classElement.getEnclosingElement().toString() + "." + classElement.getSimpleName().toString();

                        manifest.setPluginId(getPluginAnnotationValue(classElement, "value"));
                        manifest.setPluginVersion(getPluginAnnotationValue(classElement, "version"));
                        manifest.setVersionCheckUrl(getPluginAnnotationValue(classElement, "versionCheckUrl"));
                        manifest.setPluginDownloadUrl(getPluginAnnotationValue(classElement, "downloadUrl"));
                        manifest.setPluginCompatibleVersion(ApiConstants.VERSION);
                        manifest.setPluginEntryPoint(fullClassName);
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Exception while processing Ore3D annotations!");
            e.printStackTrace();
        }

        if(!Strings.isNullOrEmpty(manifest.getPluginId())) writeManifestToFile(manifest);
        else System.err.println("Valid Ore3D plugin must have a plugin ID! Invalid ID: " + manifest.getPluginId());

        return true;
    }

    private String getPluginAnnotationValue(TypeElement methodElement, String key)
    {
        for (AnnotationMirror annotationMirror : methodElement.getAnnotationMirrors())
        {
            if (annotationMirror.getAnnotationType().toString().equals(Ore3DPlugin.class.getCanonicalName()))
            {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet())
                {
                    if (entry.getKey().getSimpleName().toString().equals(key)) return (String) entry.getValue().getValue();
                }
            }
        }
        return null;
    }

    private boolean isPluginAnnotation(Element element)
    { return element.asType().toString().equals(Ore3DPlugin.class.getCanonicalName()); }

    private boolean implementsOre3DPluginInterface(TypeElement classElement)
    {
        for (TypeMirror iface : classElement.getInterfaces()) { if (iface.toString().equals(IOre3DPlugin.class.getName())) { return true; } }
        return false;
    }

    private void writeManifestToFile(Ore3DPluginManifest manifest)
    {
        try
        {
            FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE);
            try (PrintWriter out = new PrintWriter(fileObject.openWriter())) { out.write(MAPPER.writeValueAsString(manifest)); }
        }
        catch (IOException e)
        {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write plugin manifest: " + e.getMessage());
        }
    }
}
