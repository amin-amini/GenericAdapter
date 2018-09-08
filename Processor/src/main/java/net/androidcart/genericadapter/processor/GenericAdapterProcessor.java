package net.androidcart.genericadapter.processor;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import net.androidcart.genericadapter.annotations.GenericAdapter;
import net.androidcart.genericadapter.annotations.GenericAdapterView;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class GenericAdapterProcessor extends AbstractProcessor {

    ProcessingEnvironment pe;
    private Filer filer;
    private Messager messager;
    private Elements elements;

    
    private static final String PN = "net.androidcart.genericadapter";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        pe = processingEnv;
        filer = pe.getFiler();
        messager = pe.getMessager();
        elements = pe.getElementUtils();
    }


    private ClassName sectionTypeClassName = ClassName.get(PN, "SectionType");
    private ClassName sectionClassName = ClassName.get(PN, "Section");
    private ParameterizedTypeName sectionArrayList = ParameterizedTypeName.get(ClassName.get("java.util","ArrayList"), sectionClassName);

    private ClassName adapter = ClassName.get("android.support.v7.widget.RecyclerView", "Adapter");
    private ClassName viewHolder = ClassName.get("android.support.v7.widget.RecyclerView", "ViewHolder");

    private ClassName viewGroup = ClassName.get("android.view" , "ViewGroup");

    private void log(String str){
        messager.printMessage(Diagnostic.Kind.NOTE, "GenericAdapter: " + str);
    }
    private void warn(String str){
        messager.printMessage(Diagnostic.Kind.WARNING, "GenericAdapter: " + str);
    }
    void error(String str){
        messager.printMessage(Diagnostic.Kind.ERROR, "GenericAdapter: " + str);
    }

    private TypeName getModelTypeName(TypeElement typeElement){
        TypeName model = null;

        for ( TypeMirror elem : typeElement.getInterfaces()){
            try {
                ParameterizedTypeName inter = (ParameterizedTypeName) ParameterizedTypeName.get(elem);
                if (inter.rawType.equals(ClassName.get(GenericAdapterView.class))){
                    model = inter.typeArguments.get(0);
                }
            }catch (Exception ignored){}
        }
        return model;
    }


    private void addSectionMainFunctions(TypeSpec.Builder section, HashMap<TypeElement, TypeName> models){
        section.addMethod(MethodSpec.constructorBuilder()
                .addParameter(sectionTypeClassName, "type")
                .addParameter(Object.class, "data")
                .addStatement("this.type = type")
                .addStatement("this.data = data")
                .build()
        );


        section.addMethod(MethodSpec.methodBuilder("getType")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return type")
                .returns(sectionTypeClassName)
                .build());

        section.addMethod(MethodSpec.methodBuilder("setType")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(sectionTypeClassName, "type")
                .addStatement("this.type = type")
                .build());


        section.addMethod(MethodSpec.methodBuilder("getData")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return data")
                .returns(Object.class)
                .build());

        section.addMethod(MethodSpec.methodBuilder("setData")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Object.class, "data")
                .addStatement("this.data = data")
                .build());

        for ( TypeElement view : models.keySet() ){
            TypeName model = models.get(view);
            String name = view.getSimpleName().toString();

            section.addMethod(MethodSpec.methodBuilder(name)
                    .returns(sectionClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(model, "data")
                    .addStatement("return new Section(SectionType."+name+" , data)")
                    .build());

            section.addMethod(MethodSpec.methodBuilder(name)
                    .returns(sectionArrayList)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(ParameterizedTypeName.get(ClassName.get("java.util" , "Collection"), model), "data")
                    .addStatement("ArrayList<Section> ans = new ArrayList<>()")
                    .beginControlFlow("for ("+model.toString()+ " item : data)")
                        .addStatement("ans.add(new Section(SectionType."+name+" , item))")
                    .endControlFlow()
                    .addStatement("return ans")
                    .build());


        }
    }

    private void fillSectionType(TypeSpec.Builder sectionType, HashMap<TypeElement, String> packages){
        int i = 0;
        for (TypeElement typeElement : packages.keySet()){
            i++;
            sectionType.addEnumConstant(typeElement.getSimpleName().toString(),
                    TypeSpec.anonymousClassBuilder("$L", i )
                    .build());
        }

        sectionType.addField(FieldSpec.builder(TypeName.INT, "value" , Modifier.PRIVATE, Modifier.FINAL).build());

        sectionType.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(TypeName.INT, "val")
                .addStatement("this.value = val")
                .build());

        sectionType.addMethod(MethodSpec.methodBuilder("i")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(sectionTypeClassName)
                .addParameter(TypeName.INT, "x")
                .beginControlFlow( "for( SectionType st : SectionType.values() )" )
                    .beginControlFlow("if(st.value == x)")
                        .addStatement("return st")
                    .endControlFlow()
                .endControlFlow()
                .addStatement("return null")
                .build());


        sectionType.addMethod(MethodSpec.methodBuilder("getValue")
                .returns(TypeName.INT)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return value")
                .build());

        sectionType.addMethod(MethodSpec.methodBuilder("equals")
                .returns(TypeName.BOOLEAN)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "value")
                .addStatement("return this == i(value)")
                .build());
    }

    private void addDataModifiers(TypeSpec.Builder adapterClass, HashMap<TypeElement, String> packages){
        adapterClass.addMethod(MethodSpec.methodBuilder("setSections")
                .addParameter(sectionArrayList, "section")
                .addStatement("this.sections = section")
                .addStatement("notifyDataSetChanged()")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.SYNCHRONIZED)
                .build());

        adapterClass.addMethod(MethodSpec.methodBuilder("addSections")
                .addParameter(sectionArrayList, "sections")
                .addStatement("this.sections.addAll(sections)")
                .addStatement("notifyDataSetChanged()")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.SYNCHRONIZED)
                .build());

        adapterClass.addMethod(MethodSpec.methodBuilder("setSection")
                .addParameter(sectionClassName, "section")
                .addParameter(TypeName.INT, "position")
                .addStatement("this.sections.set(position, section)")
                .addStatement("notifyItemChanged(position)")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.SYNCHRONIZED)
                .build());

        adapterClass.addMethod(MethodSpec.methodBuilder("addSection")
                .addParameter(sectionClassName, "section")
                .addParameter(TypeName.INT, "position")
                .addStatement("this.sections.add(position, section)")
                .addStatement("notifyItemRangeInserted(position, 1)")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.SYNCHRONIZED)
                .build());

        adapterClass.addMethod(MethodSpec.methodBuilder("addSection")
                .addParameter(sectionClassName, "section")
                .addStatement("this.sections.add(section)")
                .addStatement("notifyItemInserted(sections.size() - 1)")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.SYNCHRONIZED)
                .build());



        adapterClass.addMethod(MethodSpec.methodBuilder("clearData")
                .addStatement("sections.clear()")
                .addStatement("notifyDataSetChanged()")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.SYNCHRONIZED)
                .build());

        adapterClass.addMethod(MethodSpec.methodBuilder("clearSectionsWithType")
                .addParameter(sectionTypeClassName, "st")
                .addStatement("ArrayList<Section> newSections = new ArrayList<>()")
                .beginControlFlow("for ( Section s : sections )")
                    .beginControlFlow("if( s.getType() != st )")
                        .addStatement("newSections.add(s)")
                    .endControlFlow()
                .endControlFlow()
                .addStatement("sections = newSections")
                .addStatement("notifyDataSetChanged()")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.SYNCHRONIZED)
                .build());
    }

    private void getItemViewType(TypeSpec.Builder adapterClass, HashMap<TypeElement, String> packages){
        MethodSpec.Builder ans = MethodSpec.methodBuilder("getItemViewType")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.SYNCHRONIZED)
                .addParameter(TypeName.INT, "position")
                .returns(TypeName.INT);

        ans.beginControlFlow("if (sections != null && 0<=sections.size() && position<sections.size())");
            ans.addStatement("return sections.get(position).getType().getValue()");
        ans.endControlFlow();
        ans.addStatement("return -1");

        adapterClass.addMethod(ans.build());
    }

    private void onCreateViewHolder(TypeSpec.Builder adapterClass, HashMap<TypeElement, TypeName> models){
        MethodSpec.Builder ans = MethodSpec.methodBuilder("onCreateViewHolder")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.SYNCHRONIZED)
                .addParameter(viewGroup, "parent")
                .addParameter(TypeName.INT, "viewType")
                .returns(viewHolder);

        ans.beginControlFlow("switch (SectionType.i(viewType))");
        for( TypeElement view : models.keySet() ){
            TypeName model = models.get(view);
            String name = view.getSimpleName().toString();
            ans.addCode("case $L:\n" , name);
            ans.addStatement("return new $LHolder(new $L(parent.getContext()))", name, name);
        }
        ans.endControlFlow();
        ans.addStatement("return null");


        adapterClass.addMethod(ans.build());
    }


    private void onBindViewHolder(TypeSpec.Builder adapterClass, HashMap<TypeElement, TypeName> models){
        MethodSpec.Builder ans = MethodSpec.methodBuilder("onBindViewHolder")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.SYNCHRONIZED)
                .addParameter(viewHolder, "holder")
                .addParameter(TypeName.INT, "position");

        ans.addStatement("int viewType = getItemViewType(position)");
        ans.beginControlFlow("if (0<=position && position < sections.size())");

        ans.addStatement("Section section = sections.get(position)");

        ans.beginControlFlow("switch (SectionType.i(viewType))");
        for( TypeElement view : models.keySet() ){
            TypeName model = models.get(view);
            String name = view.getSimpleName().toString();
            ans.addCode("case $L:\n" , name);
            ans.addStatement("(($LHolder) holder).update(($L) section.getData(), position)", name, model.toString());
            ans.addStatement("break");
        }
        ans.endControlFlow();

        ans.endControlFlow();

        adapterClass.addMethod(ans.build());
    }


    private void getItemCount(TypeSpec.Builder adapterClass){
        MethodSpec.Builder ans = MethodSpec.methodBuilder("getItemCount")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.SYNCHRONIZED)
                .returns(TypeName.INT);

        ans.addStatement("return sections.size()");
        adapterClass.addMethod(ans.build());
    }


    private void addViewHolderClasses(TypeSpec.Builder adapterClass, HashMap<TypeElement, TypeName> models){

        for( TypeElement view : models.keySet() ){
            TypeName model = models.get(view);
            TypeName viewType = ClassName.get(view.asType());
            String name = view.getSimpleName().toString();

            TypeSpec.Builder vh = TypeSpec.classBuilder(name + "Holder")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .superclass(viewHolder)
                    .addField(viewType, "view");
            vh.addMethod(MethodSpec.constructorBuilder()
                    .addParameter(viewType , "view")
                    .addStatement("super(view)")
                    .addStatement("this.view = view")
                    .build());
            vh.addMethod(MethodSpec.methodBuilder("update")
                    .addParameter(model, "model")
                    .addParameter(TypeName.INT, "position")
                    .addStatement("view.onBind(model, position)")
                    .build());
            adapterClass.addType(vh.build());
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        log("start process");

        ParameterizedTypeName adapterTypeName = ParameterizedTypeName.get(adapter, viewHolder);

        TypeSpec.Builder adapterClass = TypeSpec
                .classBuilder( "GenericRecyclerAdapter" )
                .addModifiers(Modifier.PUBLIC)
                .superclass(adapterTypeName);

        
        TypeSpec.Builder sectionType = TypeSpec.enumBuilder(sectionTypeClassName)
                .addModifiers(Modifier.PUBLIC);
        
        TypeSpec.Builder section = TypeSpec.classBuilder(sectionClassName)
                .addModifiers(Modifier.PUBLIC)
                .addField(sectionTypeClassName, "type")
                .addField(Object.class, "data");

        HashMap<TypeElement, String> packages = new HashMap<>();
        HashMap<TypeElement, TypeName> models = new HashMap<>();

        for (Element element : roundEnvironment.getElementsAnnotatedWith(GenericAdapter.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                warn("items must be a class : " + element);
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            GenericAdapter annot = typeElement.getAnnotation(GenericAdapter.class);


            String packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();

            TypeName model = getModelTypeName(typeElement);
            if (model == null){

                warn("GenericAdapter items must implement GenericAdapterView : " + typeElement);
                continue;
            }

            packages.put(typeElement, packageName);
            models.put(typeElement, model);
        }

        if (packages.size() == 0){
            return true;
        }

        fillSectionType(sectionType, packages);
        addSectionMainFunctions(section, models);

        adapterClass.addField(sectionArrayList, "sections" , Modifier.PROTECTED);

        adapterClass.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this(new ArrayList<Section>())")
                .build());

        adapterClass.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(sectionArrayList, "sections")
                .addStatement("this.sections = sections")
                .build());

        addDataModifiers(adapterClass, packages);
        getItemViewType(adapterClass, packages);
        onCreateViewHolder(adapterClass, models);
        onBindViewHolder(adapterClass, models);
        getItemCount(adapterClass);
        addViewHolderClasses(adapterClass, models);


        try {
            JavaFile.builder(PN, adapterClass.build())
                    .build()
                    .writeTo(filer);
            JavaFile.builder(PN, sectionType.build())
                    .build()
                    .writeTo(filer);
            JavaFile.builder(PN, section.build())
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(GenericAdapter.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

