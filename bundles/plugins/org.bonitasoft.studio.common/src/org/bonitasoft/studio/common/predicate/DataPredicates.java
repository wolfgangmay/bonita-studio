/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.common.predicate;

import java.util.Objects;

import org.bonitasoft.bpm.model.process.Data;

import com.google.common.base.Predicate;

public class DataPredicates {

    public static Predicate<Data> withName(final String name) {
        return new Predicate<Data>() {

            @Override
            public boolean apply(final Data input) {
                return Objects.equals(name, input.getName());
            }
        };
    }

}
