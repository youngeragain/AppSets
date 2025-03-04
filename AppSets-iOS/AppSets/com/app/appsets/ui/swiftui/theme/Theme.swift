//
//  Theme.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/4.
//
import SwiftUI
import UIKit

let LightColorPalette = ColorScheme(outline: Color(UIColor(r: 233, g: 233, b: 233, a: 214)))
let DarkColorPalette = ColorScheme(outline: Color(UIColor(r: 54, g: 54, b: 54, a: 122)))

let Theme: SwiftUITheme = SwiftUITheme(
    colorSchema: LightColorPalette,
    shapes: SwiftUIShapes(),
    typography: SwiftUITypography(),
    size: SwiftUISize()
)
