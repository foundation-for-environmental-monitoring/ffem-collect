package org.odk.collect.android.projects

import android.content.Context
import androidx.preference.PreferenceManager
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.projects.Project
import org.odk.collect.projects.Project.Saved
import org.odk.collect.projects.ProjectsRepository
import java.io.File
import java.io.FileNotFoundException

class ProjectImporter(
    private val storagePathProvider: StoragePathProvider,
    private val projectsRepository: ProjectsRepository
) {
    fun importNewProject(project: Project.New): Saved {
        val savedProject = projectsRepository.save(project)
        createProjectDirs(savedProject)
        return savedProject
    }

    fun importDemoProject() {
        val project = Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc")
        projectsRepository.save(project)
        createProjectDirs(project)
    }

    private fun createProjectDirs(project: Saved) {
        storagePathProvider.getProjectDirPaths(project.uuid).forEach { FileUtils.createDir(it) }
    }

    // Brand change
    fun updateProject(project: Saved) {
        projectsRepository.save(project)
    }

//    // Brand change
//    fun importExistingProject(): Saved {
//        return importExistingProject("")
//    }
//
//    // Brand change
//    fun importExistingProject(name: String): Saved {
//        val projectName = if (name.isEmpty()) {
//            "Existing Project"
//        } else {
//            name
//        }
//        val project = projectsRepository.save(Project.New(projectName, "E", "#3e9fcc"))
//
//        val rootDir = storagePathProvider.odkRootDirPath
//        listOf(
//            File(rootDir, "forms"),
//            File(rootDir, "instances"),
//            File(rootDir, "metadata"),
//            File(rootDir, "layers"),
//            File(rootDir, ".cache"),
//            File(rootDir, "settings")
//        ).forEach {
//            try {
//                val rootPath = storagePathProvider.getProjectRootDirPath(project.uuid)
//                org.apache.commons.io.FileUtils.moveDirectoryToDirectory(it, File(rootPath), true)
//            } catch (_: FileNotFoundException) {
//                // Original dir doesn't exist - no  need to copy
//            }
//        }
//
//        val generalSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
//        val adminSharedPrefs = context.getSharedPreferences("admin", Context.MODE_PRIVATE)
//        settingsProvider.getGeneralSettings(project.uuid).saveAll(generalSharedPrefs.all)
//        settingsProvider.getAdminSettings(project.uuid).saveAll(adminSharedPrefs.all)
//
//        createProjectDirs(project)
//        return project
//    }

    companion object {
        const val DEMO_PROJECT_ID = "DEMO"
    }
}
