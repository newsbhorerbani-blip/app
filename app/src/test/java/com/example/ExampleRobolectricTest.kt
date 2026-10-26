package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.PhoneticSearchHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("To-Let Gopalganj", appName)
  }

  @Test
  fun `phonetic search matches Bengali and English queries`() {
    assertTrue(PhoneticSearchHelper.matches("nobinbag", "নবীনবাগ বাসা"))
    assertTrue(PhoneticSearchHelper.matches("ghonapara", "ঘোনাপাড়া মেস"))
    assertTrue(PhoneticSearchHelper.matches("ভাড়া", "ফ্যামিলি বাসা rent"))
  }
}
