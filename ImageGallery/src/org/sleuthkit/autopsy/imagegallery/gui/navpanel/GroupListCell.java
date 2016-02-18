/*
 * Autopsy Forensic Browser
 *
 * Copyright 2015-16 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.imagegallery.gui.navpanel;

import static java.util.Objects.isNull;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.annotation.Nonnull;
import org.sleuthkit.autopsy.imagegallery.ImageGalleryController;
import org.sleuthkit.autopsy.imagegallery.datamodel.DrawableAttribute;
import org.sleuthkit.autopsy.imagegallery.datamodel.grouping.DrawableGroup;
import org.sleuthkit.datamodel.TagName;

/**
 *
 */
class GroupListCell extends ListCell<DrawableGroup> {

    /**
     * icon to use if this cell's TreeNode doesn't represent a group but just a
     * folder(with no DrawableFiles) in the file system hierarchy.
     */
    private static final Image EMPTY_FOLDER_ICON =
            new Image(GroupTreeCell.class.getResourceAsStream("/org/sleuthkit/autopsy/imagegallery/images/folder.png")); //NON-NLS

    /**
     * reference to group files listener that allows us to remove it from a
     * group when a new group is assigned to this Cell
     */
    private final InvalidationListener fileCountListener = (Observable o) -> {
        final String text = getGroupName() + getCountsText();
        Platform.runLater(() -> {
            setText(text);
            setTooltip(new Tooltip(text));
        });
    };

    /**
     * reference to group seen listener that allows us to remove it from a group
     * when a new group is assigned to this Cell
     */
    private final InvalidationListener seenListener = (Observable o) -> {
        final String style = getSeenStyleClass();
        Platform.runLater(() -> setStyle(style));
    };

    private final ReadOnlyObjectProperty<GroupComparators<?>> sortOrder;
    private final ImageGalleryController controller;

    GroupListCell(ImageGalleryController controller, ReadOnlyObjectProperty<GroupComparators<?>> sortOrderProperty) {
        this.controller = controller;
        this.sortOrder = sortOrderProperty;
        getStylesheets().add(GroupTreeCell.class.getResource("GroupTreeCell.css").toExternalForm()); //NON-NLS
        getStyleClass().add("groupTreeCell");        //reduce  indent to 5, default is 10 which uses up a lot of space. NON-NLS

        //since end of path is probably more interesting put ellipsis at front
        setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
        Platform.runLater(() -> prefWidthProperty().bind(getListView().widthProperty().subtract(15)));
    }

    @Override
    protected synchronized void updateItem(final DrawableGroup group, boolean empty) {
        //if there was a previous group, remove the listeners
        Optional.ofNullable(getItem())
                .ifPresent(oldGroup -> {
                    sortOrder.removeListener(fileCountListener);
            oldGroup.getFileIDs().removeListener(fileCountListener);
                    oldGroup.seenProperty().removeListener(seenListener);
            oldGroup.uncatCountProperty().removeListener(fileCountListener);
            oldGroup.hashSetHitsCountProperty().removeListener(fileCountListener);
                });

        super.updateItem(group, empty);

        if (isNull(group) || empty) {
            Platform.runLater(() -> {
                setTooltip(null);
                setText(null);
                setGraphic(null);
                setStyle("");
            });
        } else {
            final String text = getGroupName() + getCountsText();
            String style;
            Node icon;
            if (isNull(group)) {
                //"dummy" group in file system tree <=>  a folder with no drawables
                icon = new ImageView(EMPTY_FOLDER_ICON);
                style = "";
            } else {
                //if number of files in this group changes (eg a file is recategorized), update counts via listener
                group.getFileIDs().addListener(fileCountListener);
                group.uncatCountProperty().addListener(fileCountListener);
                group.hashSetHitsCountProperty().addListener(fileCountListener);
                sortOrder.addListener(fileCountListener);
                //if the seen state of this group changes update its style
                group.seenProperty().addListener(seenListener);

                //and use icon corresponding to group type
                icon = (group.getGroupByAttribute() == DrawableAttribute.TAGS)
                        ? controller.getTagsManager().getGraphic((TagName) group.getGroupByValue())
                        : group.getGroupKey().getGraphic();
                style = getSeenStyleClass();
            }

            Platform.runLater(() -> {
                setTooltip(new Tooltip(text));
                setGraphic(icon);
                setText(text);
                setStyle(style);
            });
        }
    }

    private String getGroupName() {
        return Optional.ofNullable(getItem())
                .map(group -> group.getGroupByValueDislpayName())
                .orElse("");
    }

    /**
     * return the styleClass to apply based on the assigned group's seen status
     *
     * @return the style class to apply
     */
    @Nonnull
    private String getSeenStyleClass() {
        return Optional.ofNullable(getItem())
                .map(DrawableGroup::isSeen)
                .map(seen -> seen ? "" : "-fx-font-weight:bold;") //NON-NLS
                .orElse(""); //if item is null or group is null
    }

    /**
     * get the counts part of the text to apply to this cell, including
     * parentheses
     *
     * @return get the counts part of the text to apply to this cell
     */
    @Nonnull
    private String getCountsText() {
        return Optional.ofNullable(getItem())
                .map(group ->
                        " (" + (sortOrder.get() == GroupComparators.ALPHABETICAL
                                ? group.getSize()
                                : sortOrder.get().getFormattedValueOfGroup(group)) + ")"
                ).orElse(""); //if item is null or group is null
    }
}