//
//  UIColorExtension.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/4.
//
import Foundation
import UIKit

extension UIColor {
    convenience init(r: CGFloat, g: CGFloat, b: CGFloat, a: CGFloat = 1.0) {
        let redValue = r / 255.0
        let greenValue = g / 255.0
        let blueValue = b / 255.0
        self.init(red: redValue, green: greenValue, blue: blueValue, alpha: a)
    }
}
