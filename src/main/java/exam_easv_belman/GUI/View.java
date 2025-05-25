package exam_easv_belman.GUI;

/**
 * Enumeration representing different views in the application.
 * Each enum constant is associated with an FXML file path to define its corresponding GUI layout.
 * Centralizing the mapping to each view makes it easy to add or move views without having to touch controllers.
 */
public enum View {
    LOGIN("/views/LoginView.fxml"),
    ORDER("/views/OrderView.fxml"),
    ADMIN("/views/AdminView.fxml"),
    USER_CREATION_MODAL("/views/UserCreationView.fxml"),
    CAMERA("/views/CameraView.fxml"),
    PHOTO_DOC("/views/PhotoDocView.fxml"),
    QCView("/views/QCView.fxml"),
    SEND_VIEW("/views/SendView.fxml"),
    IMG_VIEW("/views/ImageView.fxml");


    private final String FXML;

    /**
     * @param FXML the file path to the FXML file defining the GUI layout for this view
     */
    View(String FXML) {
        this.FXML = FXML;
    }

    /**
     * @return the file path of the FXML file defining the GUI layout for this view
     */
    public String getFXML() {
        return FXML;
    }
}
