/*
 *     Copyright (c) 2020 danielzhang130
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.sort_it.pythontutor

import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import ca.sort_it.pythontutor.ui.ActivityMain
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ActivityTest {
    @get:Rule
    val activityRule = ActivityTestRule(ActivityMain::class.java)

    @Test
    fun test() {
        onView(withContentDescription(R.string.open_drawer)).perform(click())
        onView(withText(R.string.intro_to_python)).perform(click())
        onView(withId(R.id.ok)).perform(click())
        onView(withId(R.id.next)).perform(click())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.last)).perform(click())
        onView(withId(R.id.prev)).perform(click())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.first)).perform(click())
        onView(withText(R.string.stdout)).perform(click())
        onView(withText(R.string.stack)).perform(click())
        onView(withText(R.string.heap)).perform(click())
        pressBack()
    }
}