package org.odk.collect.projects

import java.net.URL

object ProjectGenerator {
    fun generateProject(urlString: String, userName: String): Project.New {
        var projectName = ""
        var projectIcon = ""
        try {
            val url = URL(urlString)
            projectName = url.host
            // Brand change
            if (url.host.contains("kobotoolbox")){
                projectName = userName
            }
            projectIcon = projectName.first().toUpperCase().toString()
        } catch (e: Exception) {
        }
        return Project.New(projectName, projectIcon, "#3e9fcc")
    }
}
