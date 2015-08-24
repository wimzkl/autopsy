/*
 * Autopsy Forensic Browser
 *
 * Copyright 2015 Basis Technology Corp.
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
package org.sleuthkit.autopsy.timeline.events;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/**
 * A "local" event published by filteredEventsModel to indicate that events have
 * been(un)tagged. This event is not intended for use out side of the timeline
 * module.
 */
public class TimelineTagEvent {

    private final Set<Long> updatedEventIDs;
    private final boolean tagged;

    public ImmutableSet<Long> getUpdatedEventIDs() {
        return ImmutableSet.copyOf(updatedEventIDs);
    }

    public boolean isTagged() {
        return tagged;
    }

    public TimelineTagEvent(Set<Long> updatedEventIDs, boolean tagged) {
        this.updatedEventIDs = updatedEventIDs;
        this.tagged = tagged;
    }
}
