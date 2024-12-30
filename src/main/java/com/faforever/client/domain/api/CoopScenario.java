package com.faforever.client.domain.api;

import com.faforever.client.coop.CoopFaction;

import java.util.List;

public record CoopScenario(
    Integer id,
    String name,
    CoopFaction faction,
    List<CoopMission> maps
) {}
