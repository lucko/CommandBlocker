/*
 * Copyright (c) 2016 Lucko (Luck) <luck@lucko.me>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.commandblocker;

import com.google.inject.Inject;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "commandblocker", name = "CommandBlocker", version = "0.0.1", authors = {"Luck"})
public class CommandBlockerPlugin {

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path config;

    private final Set<String> filteredCommands = new HashSet<>();
    private Text noPermissionMessage;

    @Listener
    public void onInit(GameInitializationEvent event) {
        try {
            File cfg = config.toFile();
            cfg.getParentFile().mkdirs();

            if (!cfg.exists()) {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream("commandblocker.conf")) {
                    Files.copy(is, cfg.toPath());
                }
            }

            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                    .setPath(config)
                    .build();

            ConfigurationNode node = loader.load();
            filteredCommands.addAll(node.getNode("filtered").getList(Object::toString));
            noPermissionMessage = TextSerializers.FORMATTING_CODE.deserialize(node.getNode("block-message").getString("&cNo permission."));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onCommand(SendCommandEvent event) {
        final String name = event.getCommand().toLowerCase();

        CommandSource source = event.getCause().first(CommandSource.class).orElse(null);
        if (source == null) return;

        if (source instanceof ConsoleSource) {
            return;
        }

        boolean cancel = false;
        if (filteredCommands.contains(name)) {
            if (!source.hasPermission("commandblocker.allow." + name)) {
                cancel = true;
            }
        } else {
            if (source.hasPermission("commandblocker.block." + name)) {
                cancel = true;
            }
        }

        if (!cancel) return;

        event.setCancelled(true);
        source.sendMessage(noPermissionMessage);
    }
}
