#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
PeerReview 数据库 ER 图生成脚本

依赖:
  pip install graphviz matplotlib

Graphviz 还需安装系统程序并加入 PATH:
  Windows: https://graphviz.org/download/  或  choco install graphviz
  若未安装 Graphviz，脚本会自动回退到 matplotlib 绘制。

用法:
  python scripts/draw_er_diagram.py
  python scripts/draw_er_diagram.py -o docs/er_diagram.png
"""

from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path

# ---------------------------------------------------------------------------
# 表结构（与 sql/init.sql 一致）
# ---------------------------------------------------------------------------
TABLES: dict[str, list[tuple[str, str]]] = {
    "roles": [
        ("role_id", "PK"),
        ("role_name", "UK"),
        ("description", ""),
    ],
    "users": [
        ("user_id", "PK"),
        ("username", "UK"),
        ("password_hash", ""),
        ("real_name", ""),
        ("role_id", "FK→roles"),
        ("is_active", ""),
    ],
    "courses": [
        ("course_id", "PK"),
        ("course_name", ""),
        ("course_code", "UK"),
        ("teacher_id", "FK→users"),
        ("semester", ""),
    ],
    "course_enrollments": [
        ("enrollment_id", "PK"),
        ("course_id", "FK→courses"),
        ("user_id", "FK→users"),
        ("role_in_course", ""),
    ],
    "assignments": [
        ("assignment_id", "PK"),
        ("course_id", "FK→courses"),
        ("title", ""),
        ("due_date", ""),
        ("peer_review_count", ""),
        ("status", ""),
        ("created_by", "FK→users"),
    ],
    "rubrics": [
        ("rubric_id", "PK"),
        ("assignment_id", "FK→assignments UK"),
    ],
    "rubric_items": [
        ("item_id", "PK"),
        ("rubric_id", "FK→rubrics"),
        ("item_name", ""),
        ("max_score", ""),
        ("weight", ""),
    ],
    "submissions": [
        ("submission_id", "PK"),
        ("assignment_id", "FK→assignments"),
        ("student_id", "FK→users"),
        ("file_path", ""),
        ("content_hash", ""),
        ("similarity_pct", ""),
        ("final_score", ""),
    ],
    "peer_review_assignments": [
        ("pra_id", "PK"),
        ("assignment_id", "FK→assignments"),
        ("reviewer_id", "FK→users"),
        ("submission_id", "FK→submissions"),
        ("status", ""),
    ],
    "reviews": [
        ("review_id", "PK"),
        ("pra_id", "FK→peer_review_assignments UK"),
        ("total_score", ""),
        ("status", ""),
    ],
    "review_scores": [
        ("score_id", "PK"),
        ("review_id", "FK→reviews"),
        ("rubric_item_id", "FK→rubric_items"),
        ("score", ""),
    ],
    "appeals": [
        ("appeal_id", "PK"),
        ("pra_id", "FK→peer_review_assignments"),
        ("submission_id", "FK→submissions"),
        ("student_id", "FK→users"),
        ("handler_id", "FK→users"),
        ("status", ""),
    ],
    "discussions": [
        ("discussion_id", "PK"),
        ("assignment_id", "FK→assignments"),
        ("student_id", "FK→users"),
        ("content", ""),
    ],
    "notifications": [
        ("notification_id", "PK"),
        ("user_id", "FK→users"),
        ("title", ""),
        ("is_read", ""),
    ],
}

# (from_table, to_table, label)
RELATIONS: list[tuple[str, str, str]] = [
    ("users", "roles", "role_id"),
    ("courses", "users", "teacher_id"),
    ("course_enrollments", "courses", "course_id"),
    ("course_enrollments", "users", "user_id"),
    ("assignments", "courses", "course_id"),
    ("assignments", "users", "created_by"),
    ("rubrics", "assignments", "assignment_id"),
    ("rubric_items", "rubrics", "rubric_id"),
    ("submissions", "assignments", "assignment_id"),
    ("submissions", "users", "student_id"),
    ("peer_review_assignments", "assignments", "assignment_id"),
    ("peer_review_assignments", "users", "reviewer_id"),
    ("peer_review_assignments", "submissions", "submission_id"),
    ("reviews", "peer_review_assignments", "pra_id"),
    ("review_scores", "reviews", "review_id"),
    ("review_scores", "rubric_items", "rubric_item_id"),
    ("appeals", "peer_review_assignments", "pra_id"),
    ("appeals", "submissions", "submission_id"),
    ("appeals", "users", "student_id"),
    ("discussions", "assignments", "assignment_id"),
    ("discussions", "users", "student_id"),
    ("notifications", "users", "user_id"),
]


def _table_html(name: str, columns: list[tuple[str, str]]) -> str:
    rows = [
        f'<TR><TD COLSPAN="2" BGCOLOR="#4A90D9"><FONT COLOR="white"><B>{name}</B></FONT></TD></TR>'
    ]
    for col, tag in columns:
        tag_cell = f'<FONT POINT-SIZE="9" COLOR="#666">{tag}</FONT>' if tag else ""
        rows.append(f"<TR><TD ALIGN=\"LEFT\">{col}</TD><TD ALIGN=\"RIGHT\">{tag_cell}</TD></TR>")
    body = "".join(rows)
    return f"<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">{body}</TABLE>>"


def draw_with_graphviz(output_path: Path) -> bool:
    try:
        import graphviz
    except ImportError:
        print("未安装 graphviz 包: pip install graphviz", file=sys.stderr)
        return False

    dot = graphviz.Digraph(
        "PeerReview_ER",
        comment="PeerReview PostgreSQL ER Diagram",
        format=output_path.suffix.lstrip(".") or "png",
    )
    dot.attr(rankdir="TB", splines="ortho", nodesep="0.6", ranksep="1.0")
    dot.attr("node", shape="plaintext", fontname="Microsoft YaHei")
    dot.attr("edge", color="#555555", arrowsize="0.8", fontname="Microsoft YaHei", fontsize="9")

    for name, cols in TABLES.items():
        dot.node(name, _table_html(name, cols))

    for src, dst, label in RELATIONS:
        dot.edge(src, dst, label=label, fontsize="8")

    try:
        dot.render(output_path.with_suffix(""), cleanup=True)
        # graphviz render 输出 filename.ext
        rendered = output_path.with_suffix(f".{dot.format}")
        if rendered != output_path and rendered.exists():
            rendered.replace(output_path)
        print(f"已生成 (Graphviz): {output_path}")
        return True
    except Exception as exc:
        print(f"Graphviz 渲染失败 ({exc})，尝试 matplotlib 回退...", file=sys.stderr)
        return False


def draw_with_matplotlib(output_path: Path) -> None:
    import matplotlib.pyplot as plt
    import matplotlib.patches as mpatches
    from matplotlib.patches import FancyBboxPatch, FancyArrowPatch

    plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "DejaVu Sans"]
    plt.rcParams["axes.unicode_minus"] = False

    # 手工布局 (x, y) — 按业务域分组
    positions = {
        "roles": (1, 14),
        "users": (1, 11),
        "courses": (4, 14),
        "course_enrollments": (4, 11),
        "assignments": (7, 14),
        "rubrics": (10, 14),
        "rubric_items": (10, 11),
        "submissions": (7, 8),
        "peer_review_assignments": (4, 5),
        "reviews": (1, 5),
        "review_scores": (1, 2),
        "appeals": (7, 2),
        "discussions": (10, 8),
        "notifications": (4, 2),
    }

    fig_w, fig_h = 22, 16
    fig, ax = plt.subplots(figsize=(fig_w, fig_h))
    ax.set_xlim(0, 13)
    ax.set_ylim(0, 16)
    ax.axis("off")
    ax.set_title("PeerReview 数据库 ER 图 (PostgreSQL · 14 表)", fontsize=16, fontweight="bold", pad=20)

    box_w, line_h = 2.8, 0.28
    node_boxes: dict[str, tuple[float, float, float, float]] = {}

    for name, (cx, cy) in positions.items():
        cols = TABLES[name]
        header_h = 0.45
        body_h = len(cols) * line_h + 0.15
        total_h = header_h + body_h
        x = cx - box_w / 2
        y = cy - total_h / 2

        header = FancyBboxPatch(
            (x, y + body_h), box_w, header_h,
            boxstyle="round,pad=0.02,rounding_size=0.08",
            facecolor="#4A90D9", edgecolor="#2E5F8A", linewidth=1.2,
        )
        ax.add_patch(header)
        ax.text(cx, y + body_h + header_h / 2, name, ha="center", va="center",
                fontsize=10, fontweight="bold", color="white")

        body = FancyBboxPatch(
            (x, y), box_w, body_h,
            boxstyle="round,pad=0.02,rounding_size=0.08",
            facecolor="#F8FAFC", edgecolor="#94A3B8", linewidth=1.0,
        )
        ax.add_patch(body)

        for i, (col, tag) in enumerate(cols):
            ty = y + body_h - (i + 0.65) * line_h
            tag_str = f"  [{tag}]" if tag else ""
            ax.text(x + 0.08, ty, f"{col}{tag_str}", ha="left", va="center", fontsize=7.5, family="monospace")

        node_boxes[name] = (x, y, box_w, total_h)

    def center_top(box):
        x, y, w, h = box
        return x + w / 2, y + h

    def center_bottom(box):
        x, y, w, h = box
        return x + w / 2, y

    def center_left(box):
        x, y, w, h = box
        return x, y + h / 2

    def center_right(box):
        x, y, w, h = box
        return x + w, y + h / 2

    for src, dst, label in RELATIONS:
        sb, db = node_boxes[src], node_boxes[dst]
        sx, sy = positions[src]
        dx, dy = positions[dst]

        if abs(sy - dy) > abs(sx - dx):
            if sy > dy:
                start = center_bottom(sb)
                end = center_top(db)
            else:
                start = center_top(sb)
                end = center_bottom(db)
        else:
            if sx > dx:
                start = center_left(sb)
                end = center_right(db)
            else:
                start = center_right(sb)
                end = center_left(db)

        arrow = FancyArrowPatch(
            start, end,
            arrowstyle="-|>", mutation_scale=12,
            color="#64748B", linewidth=1.0,
            connectionstyle="arc3,rad=0.05",
        )
        ax.add_patch(arrow)
        mx, my = (start[0] + end[0]) / 2, (start[1] + end[1]) / 2
        ax.text(mx, my, label, fontsize=6, color="#475569", ha="center",
                bbox=dict(boxstyle="round,pad=0.2", facecolor="white", edgecolor="none", alpha=0.85))

    fig.text(0.5, 0.02, "图例: PK=主键  FK=外键  UK=唯一键  |  箭头指向被引用表（父表）",
             ha="center", fontsize=9, color="#64748B")

    output_path.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(output_path, dpi=150, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print(f"已生成 (matplotlib): {output_path}")


def main() -> None:
    parser = argparse.ArgumentParser(description="绘制 PeerReview 数据库 ER 图")
    parser.add_argument(
        "-o", "--output",
        default=None,
        help="输出文件路径 (默认: docs/er_diagram.png)",
    )
    parser.add_argument(
        "--format",
        choices=["png", "svg", "pdf"],
        default="png",
        help="Graphviz 输出格式 (默认 png)",
    )
    args = parser.parse_args()

    root = Path(__file__).resolve().parent.parent
    if args.output:
        output = Path(args.output)
        if not output.is_absolute():
            output = root / output
    else:
        output = root / "docs" / f"er_diagram.{args.format}"

    output.parent.mkdir(parents=True, exist_ok=True)

    if draw_with_graphviz(output.with_suffix(f".{args.format}")):
        return

    draw_with_matplotlib(output.with_suffix(".png"))


if __name__ == "__main__":
    main()
