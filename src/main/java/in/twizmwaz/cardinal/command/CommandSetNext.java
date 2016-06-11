/*
 * Copyright (c) 2016, Kevin Phoenix
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package in.twizmwaz.cardinal.command;

import com.google.common.base.Strings;
import ee.ellytr.chat.ChatConstant;
import ee.ellytr.chat.component.builder.LocalizedComponentBuilder;
import ee.ellytr.chat.component.builder.UnlocalizedComponentBuilder;
import ee.ellytr.command.Command;
import ee.ellytr.command.CommandContext;
import ee.ellytr.command.PlayerCommand;
import in.twizmwaz.cardinal.Cardinal;
import in.twizmwaz.cardinal.match.MatchThread;
import in.twizmwaz.cardinal.module.cycle.CycleModule;
import in.twizmwaz.cardinal.module.repository.LoadedMap;
import in.twizmwaz.cardinal.module.repository.RepositoryModule;
import in.twizmwaz.cardinal.util.Channels;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandSetNext {

  /**
   * Sets the next map.
   *
   * @param cmd The context of this command.
   */
  @Command(aliases = {"setnext", "sn"}, description = "Sets the next map.")
  @PlayerCommand
  public static void setNext(CommandContext cmd) {
    //TODO: This one line will be most of the method once MultiArgs are implemented.
    //Cardinal.getModule(CycleModule.class).getNextCycle().get(Cardinal.getInstance().getMatchThread()).setMap(map);
    Player player = (Player) cmd.getSender();

    String input = StringUtils.join(cmd.getArguments(), ' ').toLowerCase();
    Map<String, LoadedMap> maps = Cardinal.getModule(RepositoryModule.class).getLoadedMaps();
    Set<String> mapNames = maps.keySet();
    List<String> candidates = mapNames.stream().filter(m -> m.toLowerCase().startsWith(input))
        .collect(Collectors.toList());
    if (candidates.size() > 0) {
      String map = candidates.get(0);
      if (Strings.isNullOrEmpty(map)) {
        Channels.getPlayerChannel(player).sendMessage(
            new LocalizedComponentBuilder(ChatConstant.getConstant("cycle.set.notFound")).color(ChatColor.RED).build());
      } else {
        MatchThread matchThread = Cardinal.getMatchThread(player);
        Cardinal.getModule(CycleModule.class).getNextCycle(matchThread).setMap(maps.get(map));
        BaseComponent mapComponent = new UnlocalizedComponentBuilder(maps.get(map).getName())
            .color(ChatColor.GOLD).build();
        Channels.getPlayerChannel(player).sendMessage(new LocalizedComponentBuilder(
            ChatConstant.getConstant("cycle.set.success"), mapComponent).color(ChatColor.DARK_PURPLE).build());
      }
    } else {
      Channels.getPlayerChannel(player).sendMessage(
          new LocalizedComponentBuilder(ChatConstant.getConstant("cycle.set.notFound")).color(ChatColor.RED).build());
    }

  }

}
