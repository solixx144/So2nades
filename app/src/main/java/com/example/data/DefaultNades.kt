package com.example.data

data class DefaultNade(
    val id: String,
    val map: String,
    val type: String, // "Smoke", "Flash", "Molotov", "HE"
    val side: String, // "T", "CT"
    val title: String,
    val site: String, // "A Site", "B Site", "Mid"
    val difficulty: String, // "Easy", "Medium", "Hard"
    val throwType: String, // "Standing", "Jumpthrow", "Runthrow"
    val standingSpot: String,
    val aimSpot: String,
    val description: String,
    val videoUrl: String? = null,
    val imageUrl: String? = null
)

object DefaultNades {
    val items = listOf(
        // --- SANDSTONE ---
        DefaultNade(
            id = "sandstone_a_long_smoke",
            map = "Sandstone",
            type = "Smoke",
            side = "T",
            title = "A Long Smoke from T Spawn",
            site = "A Site",
            difficulty = "Easy",
            throwType = "Standing",
            standingSpot = "Stand at the T-Spawn stairs, wedge yourself into the right corner next to the golden banner.",
            aimSpot = "Aim at the top of the middle dome of the palace, raise crosshair slightly above the wall trim.",
            description = "Blocks off the CT Sniper checking the Long passageway, allowing full access into the A main entry.",
            videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e"
        ),
        DefaultNade(
            id = "sandstone_b_apart_smoke",
            map = "Sandstone",
            type = "Smoke",
            side = "T",
            title = "B Apartments Smoke from Alley",
            site = "B Site",
            difficulty = "Medium",
            throwType = "Standing",
            standingSpot = "Walk down the B Alley, stand exactly under the first iron lantern.",
            aimSpot = "Look up at the balcony wooden supports. Aim at the lower left edge of the third wooden strut.",
            description = "Fills apartments doorway with perfect smoke density, isolating rotates from CT spawn.",
            videoUrl = "",
            imageUrl = ""
        ),
        DefaultNade(
            id = "sandstone_a_palace_moly",
            map = "Sandstone",
            type = "Molotov",
            side = "T",
            title = "A Palace Balcony Molotov",
            site = "A Site",
            difficulty = "Medium",
            throwType = "Standing",
            standingSpot = "Stand near the Pillars entry, align yourself with the shadow line on the stone tile.",
            aimSpot = "Aim at the second window arch's lower-right edge and throw directly.",
            description = "Spreads fire across the entire balcony camping spot, forcing CT defense to drop down into the open.",
            videoUrl = "",
            imageUrl = ""
        ),
        DefaultNade(
            id = "sandstone_mid_nest_smoke",
            map = "Sandstone",
            type = "Smoke",
            side = "T",
            title = "Mid Sniper Nest Deep Smoke",
            site = "Mid",
            difficulty = "Medium",
            throwType = "Runthrow",
            standingSpot = "Stand at T-Ramp corner, align with the yellow paint mark on the concrete block.",
            aimSpot = "Aim at the topmost wiring insulator, press run key and throw after 1 step.",
            description = "Completely blanks out mid sniper window so your team can rush tunnels safely.",
            videoUrl = "",
            imageUrl = ""
        ),
        DefaultNade(
            id = "sandstone_b_backsite_he",
            map = "Sandstone",
            type = "HE",
            side = "T",
            title = "B Backsite HE Grenade Blast",
            site = "B Site",
            difficulty = "Easy",
            throwType = "Standing",
            standingSpot = "Position along the outer brick wall near B Arch, back up until the plant pot is aligned.",
            aimSpot = "Aim high above the wooden frame into the open ceiling vault.",
            description = "Deals massive explosive damage to anchor defenders holding back-site close angles.",
            videoUrl = "",
            imageUrl = ""
        ),

        // --- HANAMI ---
        DefaultNade(
            id = "hanami_sakura_smoke",
            map = "Hanami",
            type = "Smoke",
            side = "T",
            title = "Sakura Alley A-site Smoke",
            site = "A Site",
            difficulty = "Easy",
            throwType = "Standing",
            standingSpot = "Stand behind the stone fountain, align with the center wooden pillar.",
            aimSpot = "Aim at the left curved tip of the pagoda roof tile, direct left-click throw.",
            description = "Forms a protective cloud blocking the sniper alley near Sakura street.",
            videoUrl = "",
            imageUrl = ""
        ),
        DefaultNade(
            id = "hanami_b_pagoda_moly",
            map = "Hanami",
            type = "Molotov",
            side = "T",
            title = "B Pagoda Corner Molotov",
            site = "B Site",
            difficulty = "Medium",
            throwType = "Standing",
            standingSpot = "Stand at the B Gate courtyard, line up with the edge of the bamboo fence.",
            aimSpot = "Aim at the center of the wooden lantern support beam, standard standing release.",
            description = "Clears out defensive players hiding behind the dense pagoda screens.",
            videoUrl = "",
            imageUrl = ""
        ),

        // --- RUST ---
        DefaultNade(
            id = "rust_crane_smoke",
            map = "Rust",
            type = "Smoke",
            side = "T",
            title = "Crane Site Smokecover from T-Ramp",
            site = "B Site",
            difficulty = "Easy",
            throwType = "Standing",
            standingSpot = "Mount the metal ramp at Cargo area, stop at the third hazard strip.",
            aimSpot = "Aim on the yellow iron beam of the overhead crane, right above the suspension wire hooks.",
            description = "Obscures Crane camp position completely, neutralizing defender elevation advantage.",
            videoUrl = "",
            imageUrl = ""
        ),
        DefaultNade(
            id = "rust_a_vent_moly",
            map = "Rust",
            type = "Molotov",
            side = "T",
            title = "A Site Ventilation Molotov",
            site = "A Site",
            difficulty = "Easy",
            throwType = "Standing",
            standingSpot = "Position inside the dark alleyway, back yourself tight into the metallic door.",
            aimSpot = "Target the center of the spinning extraction fan intake grill.",
            description = "Forces out players trying to hold the close sneaky vent drop drop-offs.",
            videoUrl = "",
            imageUrl = ""
        ),
        DefaultNade(
            id = "rust_mid_flash",
            map = "Rust",
            type = "Flash",
            side = "CT",
            title = "Mid Courtyard Pop-Flash",
            site = "Mid",
            difficulty = "Easy",
            throwType = "Standing",
            standingSpot = "Lean against the CT command station console.",
            aimSpot = "Aim at the upper hazard mesh pane on the security window and throw.",
            description = "Flashes out any attacking players attempting to cross into Mid Main.",
            videoUrl = "",
            imageUrl = ""
        ),

        // --- PRISON ---
        DefaultNade(
            id = "prison_watchtower_smoke",
            map = "Prison",
            type = "Smoke",
            side = "CT",
            title = "Yard Guard Watchtower Smoke",
            site = "Mid",
            difficulty = "Medium",
            throwType = "Standing",
            standingSpot = "Stand at the central Yard locker doorway on the left side anchor tile.",
            aimSpot = "Look up at the barbed wire fence. Target the intersection of the support rods.",
            description = "Prevents prisoners from using high watchtower towers to survey yard movements.",
            videoUrl = "",
            imageUrl = ""
        ),
        DefaultNade(
            id = "prison_a_door_he",
            map = "Prison",
            type = "HE",
            side = "CT",
            title = "A-Block Door Entry Grenade",
            site = "A Site",
            difficulty = "Easy",
            throwType = "Standing",
            standingSpot = "Stand by the cellblocks corner hallway, face the warning sign.",
            aimSpot = "Aim at the yellow light bulb wire cages, simple hard throw.",
            description = "Flushes out early A-site rushes with heavy shrapnel AOE damage.",
            videoUrl = "",
            imageUrl = ""
        ),

        // --- DUNE ---
        DefaultNade(
            id = "dune_bazaar_moly",
            map = "Dune",
            type = "Molotov",
            side = "T",
            title = "Bazaar B-Plant Molotov",
            site = "B Site",
            difficulty = "Medium",
            throwType = "Runthrow",
            standingSpot = "Wedge behind the spice crates in the back Bazaar corridor.",
            aimSpot = "Aim at the hanging woven carpet's central medallion, run forward 2 steps and throw.",
            description = "Engulfs the default box corner in fire, instantly driving out crouching defense.",
            videoUrl = "",
            imageUrl = ""
        ),
        DefaultNade(
            id = "dune_a_gate_smoke",
            map = "Dune",
            type = "Smoke",
            side = "T",
            title = "A-Site Gateway Arch Smoke",
            site = "A Site",
            difficulty = "Easy",
            throwType = "Standing",
            standingSpot = "Stand in the middle of the sand alley corner by the wooden planks.",
            aimSpot = "Aim at the center notch of the white clay minaret steeple.",
            description = "Establishes full smoke coverage in the gate passage, blocking defender views from spawn.",
            videoUrl = "",
            imageUrl = ""
        ),

        // --- BREEZE ---
        DefaultNade(
            id = "breeze_seaside_smoke",
            map = "Breeze",
            type = "Smoke",
            side = "T",
            title = "Seaside A-Site Platform Smoke",
            site = "A Site",
            difficulty = "Hard",
            throwType = "Jumpthrow",
            standingSpot = "Wedge at the white harbor bollard on the coastal jetty.",
            aimSpot = "Aim at the top corner of the shipping crane tower, jump and release.",
            description = "Performs a deep air travel smoke landing precisely on the A platform elevated container.",
            videoUrl = "",
            imageUrl = ""
        ),
        DefaultNade(
            id = "breeze_mid_tunnel_smoke",
            map = "Breeze",
            type = "Smoke",
            side = "T",
            title = "Mid Tunnel Arch Blocking Smoke",
            site = "Mid",
            difficulty = "Easy",
            throwType = "Standing",
            standingSpot = "Stand near the boat wreckage hull, under the stern rope.",
            aimSpot = "Aim at the topmost stone block of the left arch column.",
            description = "Completely isolates tunnel crossfire so teammates can exit onto the Mid deck.",
            videoUrl = "",
            imageUrl = ""
        ),

        // --- PROVINCES ---
        DefaultNade(
            id = "provinces_ct_smoke",
            map = "Provinces",
            type = "Smoke",
            side = "T",
            title = "B CT Entrance Arch Smoke",
            site = "B Site",
            difficulty = "Easy",
            throwType = "Standing",
            standingSpot = "Stand in Arch Alley, pin yourself to the wooden cart wheel's left side.",
            aimSpot = "Aim on the stone balcony support block directly below the window plant box.",
            description = "Blocks off rotates from Fountain/CT spawn to B. Essential for A-to-B fast rotates.",
            videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            imageUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420"
        ),
        DefaultNade(
            id = "provinces_a_ramp_flash",
            map = "Provinces",
            type = "Flash",
            side = "T",
            title = "A-Ramp Pop Flashbang Burst",
            site = "A Site",
            difficulty = "Easy",
            throwType = "Standing",
            standingSpot = "Position inside B Alley, wedge tightly against the green wooden door shutter.",
            aimSpot = "Aim above the chimney rim of the opposite brick townhouse.",
            description = "Unblocks attacking path on A Site by popping in high-air to blind sniper defenders checking ramps.",
            videoUrl = "",
            imageUrl = ""
        ),
        DefaultNade(
            id = "provinces_b_site_moly",
            map = "Provinces",
            type = "Molotov",
            side = "T",
            title = "B Plant Site Corner Molotov",
            site = "B Site",
            difficulty = "Medium",
            throwType = "Standing",
            standingSpot = "Press yourself flat against the fountain basin's floral carved trim.",
            aimSpot = "Aim on the middle window plant box's right edge and release.",
            description = "Burns up the standard ninja corner on the B platform site.",
            videoUrl = "",
            imageUrl = ""
        )
    )
}
