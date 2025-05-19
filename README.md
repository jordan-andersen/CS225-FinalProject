# CS225-FinalProject

# Run

# project root
export JFX=/usr/share/openjfx/lib          # adapt to your path
export CP="out:resources:lib/*"             # runtime class-path

# 1) clean output folder
rm -rf out && mkdir out

# 2) compile every .java into out/
javac --release 17 \
      --module-path "$JFX" \
      --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.swing \
      -classpath "lib/*" \
      -d out \
      $(find src -name "*.java")

# 3) copy resources so JavaFX can find the FXML/CSS/images at runtime
cp -r resources/* out/


# 4) run

java --module-path "$JFX" \
     --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.swing \
     -classpath "$CP" \
     Main


# Compile JAR

# 0) Preconditions
#    Make sure JAVA_HOME and JFX are set correctly:
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export JFX=/usr/share/openjfx/lib


# 1) Clean up any old fat-jar bits
rm -rf build/fat build/chemical-inventory-system.jar
mkdir -p build/fat

# 2) Copy your compiled classes + resources into the fat folder
cp -r build/classes/. build/fat/

# 3) Gather all the dependency JARs (JavaFX + lib/*.jar)
mkdir -p build/fat/jars
cp "$JFX"/*.jar build/fat/jars/
cp lib/*.jar    build/fat/jars/

# 4) Inside build/fat, unpack every dependency JAR
pushd build/fat >/dev/null
for dep in jars/*.jar; do
  jar xf "$dep"
done
popd

# 5) (Optional) remove the now-redundant jars directory
rm -rf build/fat/jars

# 6) Create a minimal MANIFEST declaring your main class
mkdir -p build/fat/META-INF
cat > build/fat/META-INF/MANIFEST.MF <<EOF
Main-Class: Main
EOF

# 7) Package everything into one uber-jar
jar cfm build/chemical-inventory-system.jar \
    build/fat/META-INF/MANIFEST.MF \
    -C build/fat .

# 8) Test it
java -jar build/chemical-inventory-system.jar
