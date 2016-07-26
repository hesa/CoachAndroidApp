/******************************************************************************
 *      Copyright (c) 2015 - 2016 Henrik Sandklef
 *
 *  This file is part of Coach Assistant
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.sandklef.coachapp.filters;

import com.sandklef.coachapp.model.Media;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hesa on 2016-02-21.
 */
public class MediaFilterEngine {

    public static List<Media> apply(List<Media> list, MediaFilter bf) {
        List filteredList = new ArrayList<Media>();
        for (Media b : list) {
            if (bf.check(b)) {
                filteredList.add(b);
            }

        }
        return filteredList;
    }
}
