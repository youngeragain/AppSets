//
//  ColorSchema.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/4.
//
import UIKit
import SwiftUI

struct ColorScheme {
    let primary: Color
    let onPrimary: Color
    let primaryContainer: Color
    let onPrimaryContainer: Color
    let inversePrimary: Color
    let secondary: Color
    let onSecondary: Color
    let secondaryContainer: Color
    let onSecondaryContainer: Color
    let tertiary: Color
    let onTertiary: Color
    let tertiaryContainer: Color
    let onTertiaryContainer: Color
    let background: Color
    let onBackground: Color
    let surface: Color
    let onSurface: Color
    let surfaceVariant: Color
    let onSurfaceVariant: Color
    let surfaceTint: Color
    let inverseSurface: Color
    let inverseOnSurface: Color
    let error: Color
    let onError: Color
    let errorContainer: Color
    let onErrorContainer: Color
    let outline: Color
    let outlineVariant: Color
    let scrim: Color
    let surfaceBright: Color
    let surfaceDim: Color
    let surfaceContainer: Color
    let surfaceContainerHigh: Color
    let surfaceContainerHighest: Color
    let surfaceContainerLow: Color
    let surfaceContainerLowest: Color

    init(
        primary: Color = Color(UIColor(r: 103, g: 80, b: 164, a: 255)),
        onPrimary: Color = Color(UIColor(r: 255, g: 255, b: 255, a: 255)),
        primaryContainer: Color = Color(UIColor(r: 234, g: 221, b: 255, a: 255)),
        onPrimaryContainer: Color = Color(UIColor(r: 33, g: 0, b: 93, a: 255)),
        inversePrimary: Color = Color(UIColor(r: 208, g: 188, b: 255, a: 255)),
        secondary: Color = Color(UIColor(r: 98, g: 91, b: 113, a: 255)),
        onSecondary: Color = Color(UIColor(r: 255, g: 255, b: 255, a: 255)),
        secondaryContainer: Color = Color(UIColor(r: 232, g: 222, b: 248, a: 255)),
        onSecondaryContainer: Color = Color(UIColor(r: 29, g: 25, b: 43, a: 255)),
        tertiary: Color = Color(UIColor(r: 125, g: 82, b: 96, a: 255)),
        onTertiary: Color = Color(UIColor(r: 255, g: 255, b: 255, a: 255)),
        tertiaryContainer: Color = Color(UIColor(r: 255, g: 216, b: 228, a: 255)),
        onTertiaryContainer: Color = Color(UIColor(r: 49, g: 17, b: 29, a: 255)),
        background: Color = Color(UIColor(r: 254, g: 247, b: 255, a: 255)),
        onBackground: Color = Color(UIColor(r: 29, g: 27, b: 32, a: 255)),
        surface: Color = Color(UIColor(r: 254, g: 247, b: 255, a: 255)),
        onSurface: Color = Color(UIColor(r: 29, g: 27, b: 32, a: 255)),
        surfaceVariant: Color = Color(UIColor(r: 231, g: 224, b: 236, a: 255)),
        onSurfaceVariant: Color = Color(UIColor(r: 73, g: 69, b: 79, a: 255)),
        surfaceTint: Color = Color(UIColor(r: 103, g: 80, b: 164, a: 255)),
        inverseSurface: Color = Color(UIColor(r: 50, g: 47, b: 53, a: 255)),
        inverseOnSurface: Color = Color(UIColor(r: 245, g: 239, b: 247, a: 255)),
        error: Color = Color(UIColor(r: 179, g: 38, b: 30, a: 255)),
        onError: Color = Color(UIColor(r: 255, g: 255, b: 255, a: 255)),
        errorContainer: Color = Color(UIColor(r: 249, g: 222, b: 220, a: 255)),
        onErrorContainer: Color = Color(UIColor(r: 65, g: 14, b: 11, a: 255)),
        outline: Color = Color(UIColor(r: 121, g: 116, b: 126, a: 255)),
        outlineVariant: Color = Color(UIColor(r: 202, g: 196, b: 208, a: 255)),
        scrim: Color = Color(UIColor(r: 0, g: 0, b: 0, a: 255)),
        surfaceBright: Color = Color(UIColor(r: 254, g: 247, b: 255, a: 255)),
        surfaceDim: Color = Color(UIColor(r: 222, g: 216, b: 225, a: 255)),
        surfaceContainer: Color = Color(UIColor(r: 243, g: 237, b: 247, a: 255)),
        surfaceContainerHigh: Color = Color(UIColor(r: 236, g: 230, b: 240, a: 255)),
        surfaceContainerHighest: Color = Color(UIColor(r: 230, g: 224, b: 233, a: 255)),
        surfaceContainerLow: Color = Color(UIColor(r: 247, g: 242, b: 250, a: 255)),
        surfaceContainerLowest: Color = Color(UIColor(r: 255, g: 255, b: 255, a: 255))
    ) {
        self.primary = primary
        self.onPrimary = onPrimary
        self.primaryContainer = primaryContainer
        self.onPrimaryContainer = onPrimaryContainer
        self.inversePrimary = inversePrimary
        self.secondary = secondary
        self.onSecondary = onSecondary
        self.secondaryContainer = secondaryContainer
        self.onSecondaryContainer = onSecondaryContainer
        self.tertiary = tertiary
        self.onTertiary = onTertiary
        self.tertiaryContainer = tertiaryContainer
        self.onTertiaryContainer = onTertiaryContainer
        self.background = background
        self.onBackground = onBackground
        self.surface = surface
        self.onSurface = onSurface
        self.surfaceVariant = surfaceVariant
        self.onSurfaceVariant = onSurfaceVariant
        self.surfaceTint = surfaceTint
        self.inverseSurface = inverseSurface
        self.inverseOnSurface = inverseOnSurface
        self.error = error
        self.onError = onError
        self.errorContainer = errorContainer
        self.onErrorContainer = onErrorContainer
        self.outline = outline
        self.outlineVariant = outlineVariant
        self.scrim = scrim
        self.surfaceBright = surfaceBright
        self.surfaceDim = surfaceDim
        self.surfaceContainer = surfaceContainer
        self.surfaceContainerHigh = surfaceContainerHigh
        self.surfaceContainerHighest = surfaceContainerHighest
        self.surfaceContainerLow = surfaceContainerLow
        self.surfaceContainerLowest = surfaceContainerLowest
    }
}
