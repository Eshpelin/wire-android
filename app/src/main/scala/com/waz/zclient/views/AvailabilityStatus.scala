/**
 * Wire
 * Copyright (C) 2017 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.zclient.views

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.waz.api.impl.Availability
import com.waz.zclient.messages.UsersController
import com.waz.zclient.ui.text.{GlyphTextView, TypefaceTextView}
import com.waz.zclient.utils.ContextUtils.getString
import com.waz.zclient.{R, ViewHelper}

import com.waz.ZLog.ImplicitTag._

class AvailabilityStatus(context: Context, attrs: AttributeSet, style: Int) extends FrameLayout(context, attrs, style) with ViewHelper {
  import AvailabilityStatus._
  def this(context: Context, attrs: AttributeSet) = this(context, attrs, 0)
  def this(context: Context) = this(context, null, 0)

  inflate(R.layout.availability_status)

  private val iconView = findById[GlyphTextView](R.id.gtv__availability__icon)
  private val textView = findById[TypefaceTextView](R.id.ttv__availability__text)

  def set(av: Availability, clickable: Boolean): Unit = av match {
    case Availability.None =>
      setVisibility(View.GONE)
      if (clickable) textView.setOnClickListener(null)
    case _ =>
      setVisibility(View.VISIBLE)
      textView.setText(getResources.getString(availabilityIds(av).nameId))
      iconView.setCompoundDrawablesWithIntrinsicBounds(availabilityIds(av).iconId, 0, 0, 0)
      if (clickable) onClick { showNewAvailabilityDialog(inject[UsersController]) }
  }

  def onClick(f: => Unit): Unit = textView.setOnClickListener(new View.OnClickListener() {
    override def onClick(view: View): Unit = { f }
  })
}

object AvailabilityStatus {
  case class AvailabilityIds(nameId: Int, iconId: Int, textId: Int)

  val availabilityIds: Map[Availability, AvailabilityIds] = Map(
    Availability.None      -> AvailabilityIds(R.string.availability_none,      0,                    0),
    Availability.Available -> AvailabilityIds(R.string.availability_available, R.drawable.available, R.string.availability_text_available),
    Availability.Away      -> AvailabilityIds(R.string.availability_away,      R.drawable.away,      R.string.availability_text_away),
    Availability.Busy      -> AvailabilityIds(R.string.availability_busy,      R.drawable.busy,      R.string.availability_text_busy)
  )

  def showNewAvailabilityDialog(usersController: UsersController)(implicit ctx: Context): Unit = {
    import android.content.DialogInterface
    val availOptions = Array(Availability.None, Availability.Available, Availability.Away, Availability.Busy)
    val names: Array[CharSequence] = availOptions.map(av => getString(availabilityIds(av).nameId))

    val builder = new AlertDialog.Builder(ctx)
    builder.setItems(names, new DialogInterface.OnClickListener() {
      override def onClick(dialog: DialogInterface, which: Int): Unit = usersController.updateAvailability(availOptions(which))
    })
    builder.show()
  }
}
