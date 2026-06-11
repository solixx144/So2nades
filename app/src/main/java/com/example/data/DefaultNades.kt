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
        )
    )
}
