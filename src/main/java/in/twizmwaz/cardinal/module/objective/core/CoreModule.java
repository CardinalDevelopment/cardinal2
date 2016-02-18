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

package in.twizmwaz.cardinal.module.objective.core;

import com.google.common.collect.Lists;
import in.twizmwaz.cardinal.Cardinal;
import in.twizmwaz.cardinal.module.AbstractModule;
import in.twizmwaz.cardinal.module.ModuleError;
import in.twizmwaz.cardinal.module.objective.ProximityMetric;
import in.twizmwaz.cardinal.module.region.type.BoundedRegion;
import in.twizmwaz.cardinal.module.region.type.bounded.BlockRegion;
import in.twizmwaz.cardinal.module.team.Team;
import in.twizmwaz.cardinal.util.Materials;
import in.twizmwaz.cardinal.util.Numbers;
import in.twizmwaz.cardinal.util.ParseUtil;
import in.twizmwaz.cardinal.util.Strings;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.List;

public class CoreModule extends AbstractModule {

  private List<Core> cores;

  public CoreModule() {
    super("core");
    cores = Lists.newArrayList();
  }

  @Override
  public boolean loadMatch(Document document) {
    for (Element coresElement : document.getRootElement().getChildren("cores")) {
      for (Element coreElement : coresElement.getChildren("core")) {
        String id = ParseUtil.getFirstAttribute("id", coreElement, coresElement);

        String nameValue = ParseUtil.getFirstAttribute("name", coreElement, coresElement);
        String name = nameValue == null ? "Core" : nameValue;

        String requiredValue = ParseUtil.getFirstAttribute("required", coreElement, coresElement);
        boolean required = requiredValue == null || Numbers.parseBoolean(requiredValue);

        BoundedRegion region = null; //TODO: Get region from id
        if (region == null) {
          errors.add(new ModuleError(this, new String[]{"Invalid region specified for core"},
                  false));
          continue;
        }

        String leakValue = ParseUtil.getFirstAttribute("leak", coreElement, coresElement);
        int leak = 5;
        if (leakValue != null) {
          try {
            leak = Numbers.parseInteger(leakValue);
          } catch (NumberFormatException e) {
            errors.add(new ModuleError(this,
                    new String[]{"Invalid leak distance specified for core"}, false));
            continue;
          }
        }

        ImmutablePair<Material, Integer> material = new ImmutablePair<>(Material.OBSIDIAN, -1);
        String materialValue = ParseUtil.getFirstAttribute("material", coreElement, coresElement);
        if (materialValue != null) {
          try {
            material = Materials.getSingleMaterialPattern(materialValue);
          } catch (NumberFormatException e) {
            errors.add(new ModuleError(this,
                    new String[]{"Invalid data value of material specified for core"}, false));
            continue;
          }
        }

        Team team = null; //TODO: Get team from id
        if (team == null) {
          errors.add(new ModuleError(this, new String[]{"Invalid team specified for core"},
                  false));
          continue;
        }

        String modeChangesValue = ParseUtil.getFirstAttribute("mode-changes", coreElement,
                coresElement);
        boolean modeChanges = modeChangesValue == null || Numbers.parseBoolean(modeChangesValue);

        String showValue = ParseUtil.getFirstAttribute("show", coreElement, coresElement);
        boolean show = showValue == null || Numbers.parseBoolean(showValue);

        ProximityMetric proximityMetric = ProximityMetric.CLOSEST_PLAYER;
        String woolProximityMetricValue = ParseUtil.getFirstAttribute("proximity-metric",
                coreElement, coresElement);
        if (woolProximityMetricValue != null) {
          try {
            proximityMetric =
                    ProximityMetric.valueOf(Strings.getTechnicalName(woolProximityMetricValue));
          } catch (IllegalArgumentException e) {
            errors.add(new ModuleError(this,
                    new String[]{"Invalid proximity metric specified for core"}, false));
            continue;
          }
        }

        String proximityHorizontalValue = ParseUtil.getFirstAttribute("proximity-horizontal",
                coreElement, coresElement);
        boolean proximityHorizontal = proximityHorizontalValue != null
                && Numbers.parseBoolean(proximityHorizontalValue);

        Core core = new Core(id, name, required, region, leak, material, team, modeChanges, show,
                proximityMetric, proximityHorizontal);
        Bukkit.getPluginManager().registerEvents(core, Cardinal.getInstance());
        cores.add(core);
      }
    }
    return true;
  }

  @Override
  public void clearMatch() {
    cores.forEach(HandlerList::unregisterAll);
    cores.clear();
  }

  /**
   * @param vector The vector that this method bases the closest core off of.
   * @return The core closest to the given vector.
   */
  public Core getClosestCore(Vector vector) {
    Core closestCore = null;
    double closestDistance = Double.POSITIVE_INFINITY;
    for (Core core : cores) {
      BlockRegion center = core.getRegion().getCenterBlock();
      double distance = vector.distance(center.getVector());
      if (distance < closestDistance) {
        closestCore = core;
        closestDistance = distance;
      }
    }
    return closestCore;
  }

}