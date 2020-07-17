package org.jetbrains.research.ml.codetracker.ui.panes.util

import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.server.ServerConnectionResult

abstract class ServerDependentPane<T: PaneController> : PaneControllerManager<T>() {
    final override val canCreateContent: Boolean
        get() = PluginServer.serverConnectionResult == ServerConnectionResult.SUCCESS
}