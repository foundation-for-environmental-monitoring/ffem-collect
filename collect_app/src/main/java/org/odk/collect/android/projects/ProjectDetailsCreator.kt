package org.odk.collect.android.projects

import android.content.Context
import androidx.core.content.ContextCompat
import org.odk.collect.projects.Project
import java.net.URL
import kotlin.math.abs

class ProjectDetailsCreator(private val context: Context) {

    fun getProject(urlString: String, name: String): Project.New {
        // Brand change
        var projectName = if (name.isEmpty()) {
            "Project"
        } else {
            name
        }

        var projectIcon = "P"
        var projectColor = "#3e9fcc"
        try {
            val url = URL(urlString)
            projectName = url.host
            projectIcon = projectName.first().toUpperCase().toString()
            projectColor = getProjectColorForProjectName(projectName)
        } catch (e: Exception) {
        }
        return Project.New(projectName, projectIcon, projectColor)
    }

    private fun getProjectColorForProjectName(projectName: String): String {
        val colorId = (abs(projectName.hashCode()) % 15) + 1
        val colorName = "color$colorId"
        val colorValue = context.resources.getIdentifier(colorName, "color", context.packageName)

        return "#${Integer.toHexString(ContextCompat.getColor(context, colorValue)).substring(2)}"
    }
}
