package org.jetbrains.research.ml.tasktracker.ui.panes.util

import org.jetbrains.research.ml.tasktracker.server.PluginServer
import org.jetbrains.research.ml.tasktracker.server.ServerConnectionResult

/**
 * Represents panes that can create content only when server data is found, so they are server dependent
 */
abstract class ServerDependentPane<T: PaneController> : PaneControllerManager<T>() {
    final override val canCreateContent: Boolean
        get() = PluginServer.serverConnectionResult == ServerConnectionResult.SUCCESS
}