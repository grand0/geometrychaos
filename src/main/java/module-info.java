module ru.kpfu.itis.gr201.ponomarev.geometrychaos {
    requires javafx.controls;
    requires javafx.media;
    requires java.desktop;
    requires org.json;


    // exports for javafx runtime to launch
    exports ru.kpfu.itis.gr201.ponomarev.geometrychaos.editorapp to javafx.graphics;
    exports ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp to javafx.graphics;
}
