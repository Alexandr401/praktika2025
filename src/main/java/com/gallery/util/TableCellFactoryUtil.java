package com.gallery.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class TableCellFactoryUtil {
    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> wrappingCell() {
        return column -> new TableCell<>() {
            private final Text text = new Text();
            {
                text.wrappingWidthProperty().bind(column.widthProperty().subtract(10));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                }
            }
        };
    }

    private TableCellFactoryUtil() {}
}
