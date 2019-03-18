package com.ge.predix.mobile;

import com.ge.predix.mobile.platform.WindowView;
import javafx.scene.layout.Pane;

public interface ApplicationWindowView extends WindowView {
    void show(Pane pane);
}
