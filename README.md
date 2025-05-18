# CS225-FinalProject

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
