package com.zch.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author Zch
 * @date 2023/9/6
 **/
public class TreeNode {

    public int val;
    public int height;
    public TreeNode left;
    public TreeNode right;

    public TreeNode(int x) {
        this.val = x;
    }

    public static TreeNode listToTree(List<Integer> list) {
        int size = list.size();
        if (size == 0) {
            return null;
        }

        TreeNode root = new TreeNode(list.get(0));
        Queue<TreeNode> queue = new LinkedList<>() {{ add(root); }};
        int i = 0;
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            if (++i >= size) {
                break;
            }
            if (list.get(i) != null) {
                node.left = new TreeNode(list.get(i));
                queue.add(node.left);
            }
            if (++i >= size) {
                break;
            }
            if (list.get(i) != null) {
                node.right = new TreeNode(list.get(i));
                queue.add(node.right);
            }
        }
        return root;
    }

    /* Serialize a binary tree to a list */
    public static List<Integer> treeToList(TreeNode root) {
        List<Integer> list = new ArrayList<>();
        if (root == null) {
            return list;
        }
        Queue<TreeNode> queue = new LinkedList<>() {{ add(root); }};
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            if (node != null) {
                list.add(node.val);
                queue.add(node.left);
                queue.add(node.right);
            } else {
                list.add(null);
            }
        }
        return list;
    }
}
